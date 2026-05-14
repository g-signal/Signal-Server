/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

import java.time.Instant;

/**
 * 出参：/v1/linkbapay/link/getLinkedBaUserInfo 返回。
 * 字段对应 t_signal_user.linkbaxs_*。未绑定时除 baxsAppUserId 外均为 null。
 */
public record GetLinkedBaUserInfoResponse(
        String baxsAppUserId,
        String linkbaxsOptId,
        String linkbaxsMemberId,
        String linkbaxsOptName,
        String linkbaxsOptEmail,
        String linkbaxsOptMobile,
        Instant linkbaxsDate) {
}
