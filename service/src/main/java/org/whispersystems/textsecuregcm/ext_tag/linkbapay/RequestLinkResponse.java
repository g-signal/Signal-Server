/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * API 3 出参：/v1/linkbapay/link/requestLink 返回。linkStatus 固定 2(SCANNED)。
 */
public record RequestLinkResponse(
        String linkId,
        String optId,
        String memberId,
        String baxsAppUserId,
        Integer linkStatus,
        String linkStatusName,
        Instant expireTime) {
}
