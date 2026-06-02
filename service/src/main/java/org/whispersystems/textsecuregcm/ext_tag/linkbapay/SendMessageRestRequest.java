/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Date;

/**
 * REST-layer body for POST /v1/linkbapay/message/send.
 * baxsAppUserId is intentionally omitted — the controller fills it from the authenticated account.
 */
public record SendMessageRestRequest(
        @NotBlank String baxsMessageId,
        @NotBlank String optId,
        @NotNull Integer messageType,
        String content,
        String fileUrl,
        String fileName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        Date sendTime) {
}
