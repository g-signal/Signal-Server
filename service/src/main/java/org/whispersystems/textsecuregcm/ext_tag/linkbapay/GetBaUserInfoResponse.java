/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * API 2 出参：/v1/linkbapay/link/getBaUserInfo 返回。
 */
public record GetBaUserInfoResponse(
        String linkId,
        String optId,
        String memberId,
        String optName,
        String email,
        String mobile,
        Integer linkStatus,
        String linkStatusName,
        Boolean canRequestLink,
        String failReason,
        Instant expireTime) {
}
