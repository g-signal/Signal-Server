/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay;

/**
 * API 5 入参：调用 POST /v1/linkbapay/link/getLinkResult。
 */
public record GetLinkResultRequest(String baxsAppUserId,String linkId) {
}
