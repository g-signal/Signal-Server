/*
 * Copyright 2013 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.auth;

import static com.codahale.metrics.MetricRegistry.name;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicCredentials;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.whispersystems.textsecuregcm.identity.IdentityType;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;
import org.whispersystems.textsecuregcm.util.Pair;
import org.whispersystems.textsecuregcm.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountAuthenticator implements Authenticator<BasicCredentials, AuthenticatedDevice> {

  private static final Logger logger = LoggerFactory.getLogger(AccountAuthenticator.class);

  private static final String LEGACY_NAME_PREFIX = "org.whispersystems.textsecuregcm.auth.BaseAccountAuthenticator";

  private static final String AUTHENTICATION_COUNTER_NAME = name(LEGACY_NAME_PREFIX, "authentication");
  private static final String AUTHENTICATION_SUCCEEDED_TAG_NAME = "succeeded";
  private static final String AUTHENTICATION_FAILURE_REASON_TAG_NAME = "reason";

  private static final String DAYS_SINCE_LAST_SEEN_DISTRIBUTION_NAME = name(LEGACY_NAME_PREFIX, "daysSinceLastSeen");
  private static final String IS_PRIMARY_DEVICE_TAG = "isPrimary";

  private static final Counter OLD_TOKEN_VERSION_COUNTER =
      Metrics.counter(name(AccountAuthenticator.class, "oldTokenVersionCounter"));

  @VisibleForTesting
  static final char DEVICE_ID_SEPARATOR = '.';

  private final AccountsManager accountsManager;
  private final Clock clock;

  public AccountAuthenticator(AccountsManager accountsManager) {
    this(accountsManager, Clock.systemUTC());
  }

  @VisibleForTesting
  public AccountAuthenticator(AccountsManager accountsManager, Clock clock) {
    this.accountsManager = accountsManager;
    this.clock = clock;
  }

  static Pair<String, Byte> getIdentifierAndDeviceId(final String basicUsername) {
    final String identifier;
    final byte deviceId;

    final int deviceIdSeparatorIndex = basicUsername.indexOf(DEVICE_ID_SEPARATOR);

    if (deviceIdSeparatorIndex == -1) {
      identifier = basicUsername;
      deviceId = Device.PRIMARY_ID;
    } else {
      identifier = basicUsername.substring(0, deviceIdSeparatorIndex);
      deviceId = Byte.parseByte(basicUsername.substring(deviceIdSeparatorIndex + 1));
    }

    return new Pair<>(identifier, deviceId);
  }

  @Override
  public Optional<AuthenticatedDevice> authenticate(BasicCredentials basicCredentials) {
    boolean succeeded = false;
    String failureReason = null;
    UUID accountUuid = null;
    byte deviceId = -1;

    try {
      logger.debug("Authentication attempt for username: {}", basicCredentials.getUsername());

      {
        final Pair<String, Byte> identifierAndDeviceId = getIdentifierAndDeviceId(basicCredentials.getUsername());

        accountUuid = UUID.fromString(identifierAndDeviceId.first());
        deviceId = identifierAndDeviceId.second();

        logger.debug("Parsed accountUuid: {}, deviceId: {}", accountUuid, deviceId);
      }

      Optional<Account> account = accountsManager.getByAccountIdentifier(accountUuid);

      if (account.isEmpty()) {
        failureReason = "noSuchAccount";
        logger.warn("Authentication failed: Account not found for UUID: {}", accountUuid);
        return Optional.empty();
      }

      logger.debug("Account found for UUID: {}, number: {}", accountUuid, account.get().getNumber());

      Optional<Device> device = account.get().getDevice(deviceId);

      if (device.isEmpty()) {
        failureReason = "noSuchDevice";
        logger.warn("Authentication failed: Device {} not found for account: {}", deviceId, accountUuid);
        return Optional.empty();
      }

      logger.debug("Device found: id={}, lastSeen={}", device.get().getId(), device.get().getLastSeen());

      SaltedTokenHash deviceSaltedTokenHash = device.get().getAuthTokenHash();
      String providedPassword = basicCredentials.getPassword();

      logger.debug("Token verification: stored hash version={}, provided password length={}",
          deviceSaltedTokenHash.getVersion(), providedPassword != null ? providedPassword.length() : 0);

      if (deviceSaltedTokenHash.verify(basicCredentials.getPassword())) {
        succeeded = true;
        logger.info("Authentication successful for account: {}, device: {}", accountUuid, deviceId);
        Account authenticatedAccount = updateLastSeen(account.get(), device.get());
        if (deviceSaltedTokenHash.getVersion() != SaltedTokenHash.CURRENT_VERSION) {
          OLD_TOKEN_VERSION_COUNTER.increment();
          logger.debug("Updating token version from {} to {}", deviceSaltedTokenHash.getVersion(), SaltedTokenHash.CURRENT_VERSION);
          authenticatedAccount = accountsManager.updateDeviceAuthentication(
              authenticatedAccount,
              device.get(),
              SaltedTokenHash.generateFor(basicCredentials.getPassword()));  // new credentials have current version
        }
        return Optional.of(new AuthenticatedDevice(authenticatedAccount.getIdentifier(IdentityType.ACI),
            device.get().getId(),
            Instant.ofEpochMilli(authenticatedAccount.getPrimaryDevice().getLastSeen())));
      } else {
        failureReason = "incorrectPassword";
        logger.warn("Authentication failed: Password verification failed for account: {}, device: {}", accountUuid, deviceId);
        return Optional.empty();
      }
    } catch (IllegalArgumentException | InvalidAuthorizationHeaderException iae) {
      failureReason = "invalidHeader";
      logger.warn("Authentication failed: Invalid header format - {}", iae.getMessage());
      return Optional.empty();
    } finally {
      Tags tags = Tags.of(
          AUTHENTICATION_SUCCEEDED_TAG_NAME, String.valueOf(succeeded));

      if (StringUtils.isNotBlank(failureReason)) {
        tags = tags.and(AUTHENTICATION_FAILURE_REASON_TAG_NAME, failureReason);
        logger.debug("Authentication result: failed with reason '{}' for account: {}, device: {}",
            failureReason, accountUuid, deviceId);
      }

      Metrics.counter(AUTHENTICATION_COUNTER_NAME, tags).increment();
    }
  }

  @VisibleForTesting
  public Account updateLastSeen(Account account, Device device) {
    // compute a non-negative integer between 0 and 86400.
    long n = Util.ensureNonNegativeLong(account.getUuid().getLeastSignificantBits());
    final long lastSeenOffsetSeconds = n % ChronoUnit.DAYS.getDuration().toSeconds();

    // produce a truncated timestamp which is either today at UTC midnight
    // or yesterday at UTC midnight, based on per-user randomized offset used.
    final long todayInMillisWithOffset = Util.todayInMillisGivenOffsetFromNow(clock,
        Duration.ofSeconds(lastSeenOffsetSeconds).negated());

    // only update the device's last seen time when it falls behind the truncated timestamp.
    // this ensures a few things:
    //   (1) each account will only update last-seen at most once per day
    //   (2) these updates will occur throughout the day rather than all occurring at UTC midnight.
    if (device.getLastSeen() < todayInMillisWithOffset) {
      Metrics.summary(DAYS_SINCE_LAST_SEEN_DISTRIBUTION_NAME, IS_PRIMARY_DEVICE_TAG, String.valueOf(device.isPrimary()))
          .record(Duration.ofMillis(todayInMillisWithOffset - device.getLastSeen()).toDays());

      return accountsManager.updateDeviceLastSeen(account, device, Util.todayInMillis(clock));
    }

    return account;
  }
}
