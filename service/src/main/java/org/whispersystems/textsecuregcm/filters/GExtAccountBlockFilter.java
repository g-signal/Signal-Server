/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.filters;

import io.dropwizard.auth.Auth;
import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import java.util.Optional;
import java.util.UUID;
import org.whispersystems.textsecuregcm.auth.AuthenticatedDevice;
import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicConfiguration;
import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicGExtAccountBlockConfiguration;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.DynamicConfigurationManager;

@Priority(Priorities.AUTHORIZATION)
public class GExtAccountBlockFilter implements ContainerRequestFilter {

  private final DynamicConfigurationManager<DynamicConfiguration> dynamicConfigurationManager;
  private final AccountsManager accountsManager;

  private static final String BLOCKED_ACCOUNT_COUNTER_NAME = "gext.account.blocked";

  public GExtAccountBlockFilter(
      final DynamicConfigurationManager<DynamicConfiguration> dynamicConfigurationManager,
      final AccountsManager accountsManager) {
    this.dynamicConfigurationManager = dynamicConfigurationManager;
    this.accountsManager = accountsManager;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    final Optional<AuthenticatedDevice> maybeAuthenticatedDevice =
        Optional.ofNullable((AuthenticatedDevice) requestContext.getProperty("auth"));

    if (maybeAuthenticatedDevice.isEmpty()) {
      return;
    }

    final AuthenticatedDevice authenticatedDevice = maybeAuthenticatedDevice.get();
    final Optional<Account> maybeAccount = accountsManager.getByAccountIdentifier(
        authenticatedDevice.accountIdentifier());

    if (maybeAccount.isEmpty()) {
      return;
    }

    final Account account = maybeAccount.get();
    final DynamicGExtAccountBlockConfiguration config =
        dynamicConfigurationManager.getConfiguration().getGextAccountBlockConfiguration();

    if (!config.isGextBlockEnabled()) {
      return;
    }

    boolean isBlocked = false;

    if (config.getGextBlockedAccountUuids().contains(account.getUuid())) {
      isBlocked = true;
    }

    if (config.getGextBlockedPhoneNumbers().contains(account.getNumber())) {
      isBlocked = true;
    }

    if (isBlocked) {
      Metrics.counter(BLOCKED_ACCOUNT_COUNTER_NAME,
          "uuid", account.getUuid().toString()).increment();
      requestContext.abortWith(Response.status(403).build());
    }
  }
}
