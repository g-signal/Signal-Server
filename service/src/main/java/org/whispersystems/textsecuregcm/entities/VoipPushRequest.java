/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record VoipPushRequest(
    @JsonProperty("callMessageRelayPayload")
    @NotBlank
    String callMessageRelayPayload
) {
}