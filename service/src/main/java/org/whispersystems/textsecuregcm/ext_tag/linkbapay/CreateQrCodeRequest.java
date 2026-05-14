/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 1 入参：调用 POST /v1/linkbapay/link/qrcode/create。
 */
public record CreateQrCodeRequest(
        String optId,
        String memberId,
        String optName,
        String email,
        String mobile) {
}
