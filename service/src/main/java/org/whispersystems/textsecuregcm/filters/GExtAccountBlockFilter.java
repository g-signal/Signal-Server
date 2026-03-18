/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.filters;

import io.micrometer.core.instrument.Metrics;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import java.util.UUID;
import org.whispersystems.textsecuregcm.auth.AuthenticatedDevice;
import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicConfiguration;
import org.whispersystems.textsecuregcm.configuration.dynamic.DynamicGExtAccountBlockConfiguration;
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
    final SecurityContext securityContext = requestContext.getSecurityContext();

    if (securityContext == null || securityContext.getUserPrincipal() == null) {
      return;
    }

    if (!(securityContext.getUserPrincipal() instanceof AuthenticatedDevice authenticatedDevice)) {
      return;
    }

    final DynamicGExtAccountBlockConfiguration config =
        dynamicConfigurationManager.getConfiguration().getGextAccountBlockConfiguration();

    if (!config.isEnabled()) {
      return;
    }

    final UUID accountUuid = authenticatedDevice.accountIdentifier();

    if (config.getBlockedAccountUuids().contains(accountUuid)) {
      Metrics.counter(BLOCKED_ACCOUNT_COUNTER_NAME,
          "uuid", accountUuid.toString(),
          "reason", "uuid_blocked").increment();
      requestContext.abortWith(Response.status(403).build());
      return;
    }

    accountsManager.getByAccountIdentifier(accountUuid).ifPresent(account -> {
      if (config.getBlockedPhoneNumbers().contains(account.getNumber())) {
        Metrics.counter(BLOCKED_ACCOUNT_COUNTER_NAME,
            "uuid", accountUuid.toString(),
            "reason", "phone_blocked").increment();
        requestContext.abortWith(Response.status(403).build());
      }
    });
  }
}
