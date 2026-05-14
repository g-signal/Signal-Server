/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 6 出参：/v1/linkbapay/message/send 返回。
 */
public record SendMessageResponse(Boolean success, String reason) {
}
