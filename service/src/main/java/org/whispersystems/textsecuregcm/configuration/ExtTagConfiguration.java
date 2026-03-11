/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.whispersystems.textsecuregcm.configuration.secrets.SecretBytes;
import org.whispersystems.textsecuregcm.util.ExactlySize;

import java.util.List;

public record ExtTagConfiguration(
    @NotBlank String uri,
    @NotNull Boolean svrCaCertificatesEnabled,
    List<@NotBlank String> svrCaCertificates,
    @NotNull @Valid CircuitBreakerConfiguration circuitBreaker,
    @NotNull @Valid RetryConfiguration retry) {

  public ExtTagConfiguration {
    if (circuitBreaker == null) {
      circuitBreaker = new CircuitBreakerConfiguration();
    }

    if (retry == null) {
      retry = new RetryConfiguration();
    }
  }
}
