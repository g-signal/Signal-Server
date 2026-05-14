/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 2 入参：调用 POST /v1/linkbapay/link/getBaUserInfo。
 */
public record GetBaUserInfoRequest(String linkId) {
}
