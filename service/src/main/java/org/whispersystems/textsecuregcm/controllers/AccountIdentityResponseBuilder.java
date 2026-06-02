/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.controllers;

import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicGExtAccountBlockConfiguration;
import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicGExtRobotConfiguration;
import org.whispersystems.textsecuregcm.entities.AccountIdentityResponse;
import org.whispersystems.textsecuregcm.entities.Entitlements;
import org.whispersystems.textsecuregcm.ext_robot.GextRobot;
import org.whispersystems.textsecuregcm.ext_tag.GextTag;
import org.whispersystems.textsecuregcm.ext_tag.GextTagClient;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.DeviceCapability;
import org.whispersystems.textsecuregcm.util.ProfileHelper;

public class AccountIdentityResponseBuilder {

  private final Account account;
  private boolean storageCapable;
  private Clock clock;
  private GextTagClient extTagClient;
  private DynamicGExtRobotConfiguration gExtRobotConfiguration;

  public AccountIdentityResponseBuilder(Account account) {
    this.account = account;
    this.storageCapable = account.hasCapability(DeviceCapability.STORAGE);
    this.clock = Clock.systemUTC();
  }

  public AccountIdentityResponseBuilder storageCapable(boolean storageCapable) {
    this.storageCapable = storageCapable;
    return this;
  }

  public AccountIdentityResponseBuilder clock(Clock clock) {
    this.clock = clock;
    return this;
  }

  public AccountIdentityResponseBuilder gExtRobotConfiguration(DynamicGExtRobotConfiguration gExtRobotConfiguration) {
    this.gExtRobotConfiguration = gExtRobotConfiguration;
    return this;
  }

  public AccountIdentityResponseBuilder extTagClient(GextTagClient extTagClient) {
    this.extTagClient = extTagClient;
    return this;
  }

  public AccountIdentityResponse build() {
    final List<Entitlements.BadgeEntitlement> badges = account.getBadges()
        .stream()
        .filter(bv -> bv.expiration().isAfter(clock.instant()))
        .map(badge -> new Entitlements.BadgeEntitlement(badge.id(), badge.expiration(), badge.visible()))
        .toList();

    final Entitlements.BackupEntitlement backupEntitlement = Optional
        .ofNullable(account.getBackupVoucher())
        .filter(bv -> bv.expiration().isAfter(clock.instant()))
        .map(bv -> new Entitlements.BackupEntitlement(bv.receiptLevel(), bv.expiration()))
        .orElse(null);

    final List<GextTag> extTags = extTagClient != null
        ? ProfileHelper.queryExternalTags(extTagClient, account.getUuid())
        : Collections.emptyList();

    final GextRobot gextRobot = ProfileHelper.testGextRobot(this.gExtRobotConfiguration, account.getUuid());

    return new AccountIdentityResponse(account.getUuid(),
        account.getNumber(),
        account.getPhoneNumberIdentifier(),
        account.getUsernameHash().filter(h -> h.length > 0).orElse(null),
        account.getUsernameLinkHandle(),
        storageCapable,
        new Entitlements(badges, backupEntitlement),
        extTags,
        gextRobot);
  }

  public static AccountIdentityResponse fromAccount(final Account account) {
    return new AccountIdentityResponseBuilder(account).build();
  }

  public static AccountIdentityResponse fromAccount(final Account account, final GextTagClient extTagClient, final DynamicGExtRobotConfiguration gExtRobotConfiguration) {
    return new AccountIdentityResponseBuilder(account).extTagClient(extTagClient).gExtRobotConfiguration(gExtRobotConfiguration).build();
  }
}
