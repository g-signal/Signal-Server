/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 4 入参：调用 POST /v1/linkbapay/link/confirm。
 * confirmResult=true 同意；false 拒绝。
 */
public record ConfirmLinkRequest(
        String linkId,
        String optId,
        Boolean confirmResult,
        String failReason) {
}
