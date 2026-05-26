/*
 * Copyright 2026 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag.linkbapay.rest;

import jakarta.validation.constraints.NotBlank;

/**
 * REST-layer body for POST /v1/linkbapay/link/requestLink.
 * Auth-derived fields (baxsAppUserId / baxsAppUserName / baxsAppUserMobile) are intentionally
 * not accepted from the client — the controller fills them from the authenticated account.
 * Only the email is left optional on the wire because Signal-Server does not store user email.
 */
public record RequestLinkRestRequest(
        @NotBlank String linkId,
        @NotBlank Integer confirmResult,
        String failReason) {
}
