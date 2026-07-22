/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.configuration;

import jakarta.validation.constraints.NotNull;
import org.whispersystems.textsecuregcm.configuration.secrets.SecretString;
import javax.annotation.Nullable;

public record HlrLookupConfiguration(@NotNull Boolean enabled,
                                     SecretString apiKey,
                                     SecretString apiSecret,
                                     @Nullable String circuitBreakerConfigurationName,
                                     @Nullable String retryConfigurationName) {
}
