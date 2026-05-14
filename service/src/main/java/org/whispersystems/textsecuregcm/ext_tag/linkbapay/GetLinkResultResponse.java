/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * API 5 出参：/v1/linkbapay/link/getLinkResult 返回。linkStatus 可能为 1-5 任一状态。
 */
public record GetLinkResultResponse(
        String linkId,
        String optId,
        String memberId,
        String baxsAppUserId,
        Integer linkStatus,
        String linkStatusName,
        Instant confirmTime,
        String failReason) {
}
