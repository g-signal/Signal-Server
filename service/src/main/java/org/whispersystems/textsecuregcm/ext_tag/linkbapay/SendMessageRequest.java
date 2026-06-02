/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;
import java.util.Date;

/**
 * API 6 入参：调用 POST /v1/linkbapay/message/send。
 * messageType=1 必填 content；2/3 必填 fileUrl；2 必填 fileName。
 */
public record SendMessageRequest(
        String baxsMessageId,
        String baxsAppUserId,
        String optId,
        Integer messageType,
        String content,
        String fileUrl,
        String fileName,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        Date sendTime) {
}
