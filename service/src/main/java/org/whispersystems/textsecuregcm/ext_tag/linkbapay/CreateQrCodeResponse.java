/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * API 1 出参：/v1/linkbapay/link/qrcode/create 返回。
 * linkStatus 仅可能为 1(PENDING_SCAN) 或 2(SCANNED)。
 */
public record CreateQrCodeResponse(
        String linkId,
        String qrCodeUrl,
        String qrCodeContent,
        Integer linkStatus,
        String linkStatusName,
        Instant expireTime) {
}
