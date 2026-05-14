/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * API 4 出参：/v1/linkbapay/link/confirm 返回。linkStatus 可能为 3(LINKED)、4(FAILED)、5(TIMEOUT)。
 */
public record ConfirmLinkResponse(
        String linkId,
        String optId,
        String memberId,
        String baxsAppUserId,
        Integer linkStatus,
        String linkStatusName,
        Instant confirmTime) {
}
