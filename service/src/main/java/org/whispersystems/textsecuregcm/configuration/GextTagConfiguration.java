/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public record GextTagConfiguration(
    @NotBlank String uri,
    @NotNull Boolean extTagCaCertificatesEnabled,
    List<@NotBlank String> extTagCaCertificates,
    @Nullable String circuitBreakerConfigurationName,
    @Nullable String retryConfigurationName) {
}
