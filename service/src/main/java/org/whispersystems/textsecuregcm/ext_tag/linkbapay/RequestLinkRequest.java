/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 3 入参：调用 POST /v1/linkbapay/link/requestLink。
 */
public record RequestLinkRequest(
        String linkId,
        String baxsAppUserId,
        String baxsAppUserName,
        String baxsAppUserMobile,
        String baxsAppUserEmail,
        Boolean confirmResult,
        String failReason) {
}
