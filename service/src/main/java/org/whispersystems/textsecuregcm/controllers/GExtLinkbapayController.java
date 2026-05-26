/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.controllers;

import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.AuthenticatedDevice;
import org.whispersystems.textsecuregcm.ext_tag.GextTagClient;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetBaUserInfoRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetBaUserInfoResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkResultRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkResultResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkedBaUserInfoRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkedBaUserInfoResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.RequestLinkRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.RequestLinkResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.rest.GetLinkResultRestRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.rest.RequestLinkRestRequest;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;

import java.util.concurrent.CompletableFuture;

/**
 * REST wrapper around {@link GextTagClient}'s six BAXS / Bapay linking and messaging endpoints.
 *
 * All endpoints require an authenticated account (Dropwizard {@code @Auth AuthenticatedDevice}).
 * For {@code requestLink} and {@code sendMessage}, the BAXS app-user identity fields
 * (baxsAppUserId / baxsAppUserName / baxsAppUserMobile) are sourced from the authenticated
 * Signal account; the client cannot supply or override them.
 *
 * Style mirrors {@link DeviceController}: JAX-RS annotations, {@code @Auth} for auth,
 * async via {@link CompletableFuture}, upstream failure surfaces as 502.
 */
@Path("/v1/gext/linkbapay")
@Tag(name = "Linkbapay")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GExtLinkbapayController {

    private static final Logger log = LoggerFactory.getLogger(GExtLinkbapayController.class);

    private final AccountsManager accounts;
    private final GextTagClient extTagClient;

    public GExtLinkbapayController(final AccountsManager accounts, final GextTagClient extTagClient) {
        this.accounts = accounts;
        this.extTagClient = extTagClient;
    }


    /** API 2: fetch the BA operator info bound to a linkId. */
    @POST
    @Path("/link/getBaUserInfo")
    @Operation(summary = "Fetch the BA operator info bound to a linkId")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Caller is not authenticated")
    @ApiResponse(responseCode = "502", description = "BAXS upstream failure")
    public CompletableFuture<GetBaUserInfoResponse> getBaUserInfo(
            @Auth final AuthenticatedDevice auth,
            @NotNull @Valid final GetBaUserInfoRequest request) {

        return extTagClient.getBaUserInfo(request).thenApply(this::orUpstream502);
    }

    /**
     * API 3: submit a binding request after scanning a QR code.
     * baxsAppUserId / baxsAppUserName / baxsAppUserMobile are filled from the authenticated
     * Signal account; the client only supplies linkId (and optionally an email).
     */
    @POST
    @Path("/link/requestLink")
    @Operation(summary = "Submit a BAXS binding request as the authenticated account")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Caller is not authenticated")
    @ApiResponse(responseCode = "502", description = "BAXS upstream failure")
    public CompletableFuture<RequestLinkResponse> requestLink(
            @Auth final AuthenticatedDevice auth,
            @NotNull @Valid final RequestLinkRestRequest body) {

        final Account account = requireAccount(auth);

        String baxsAppUserId = null;
        String baxsAppUserName = null;
        String baxsAppUserMobile = null;
        String baxsAppUserEmail = null;

        if(body.confirmResult()!=null && body.confirmResult()){
            baxsAppUserId = account.getUuid().toString();
            baxsAppUserName = body.userName();
            baxsAppUserMobile = account.getNumber();
        }

        final RequestLinkRequest upstream = new RequestLinkRequest(
                body.linkId(),
                baxsAppUserId,
                // Signal stores display names encrypted on the client; no plaintext name available server-side.
                baxsAppUserName,
                baxsAppUserMobile,
                baxsAppUserEmail,
                body.confirmResult()!=null && body.confirmResult(),
                body.failReason());

        return extTagClient.requestLink(upstream).thenApply(this::orUpstream502);
    }


    /** API 5: poll the final state of a binding request. */
    @POST
    @Path("/link/getLinkResult")
    @Operation(summary = "Poll the result of a BAXS binding request")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Caller is not authenticated")
    @ApiResponse(responseCode = "502", description = "BAXS upstream failure")
    public CompletableFuture<GetLinkResultResponse> getLinkResult(
            @Auth final AuthenticatedDevice auth,
            @NotNull @Valid final GetLinkResultRestRequest request) {

        final Account account = requireAccount(auth);

        final GetLinkResultRequest upstream = new GetLinkResultRequest(
                account.getUuid().toString(),
                request.linkId());

        return extTagClient.getLinkResult(upstream).thenApply(this::orUpstream502);
    }

    /**
     * Fetch the BA operator info already bound to the authenticated Signal account.
     * baxsAppUserId is sourced from auth; the client cannot supply or override it,
     * so this endpoint takes no request body.
     */
    @POST
    @Path("/link/getLinkedBaUserInfo")
    @Operation(summary = "Fetch the BA operator already bound to the authenticated account")
    @ApiResponse(responseCode = "200", useReturnTypeSchema = true)
    @ApiResponse(responseCode = "401", description = "Caller is not authenticated")
    @ApiResponse(responseCode = "502", description = "BAXS upstream failure")
    public CompletableFuture<GetLinkedBaUserInfoResponse> getLinkedBaUserInfo(
            @Auth final AuthenticatedDevice auth) {

        final Account account = requireAccount(auth);

        final GetLinkedBaUserInfoRequest upstream = new GetLinkedBaUserInfoRequest(
                account.getUuid().toString());

        return extTagClient.getLinkedBaUserInfo(upstream).thenApply(this::orUpstream502);
    }

    private Account requireAccount(final AuthenticatedDevice auth) {
        return accounts.getByAccountIdentifier(auth.accountIdentifier())
                .orElseThrow(() -> new WebApplicationException(Response.Status.UNAUTHORIZED));
    }

    private <T> T orUpstream502(final T result) {
        if (result == null) {
            log.warn("BAXS upstream returned no result, surfacing 502");
            throw new WebApplicationException("BAXS upstream failure", Response.Status.BAD_GATEWAY);
        }
        return result;
    }
}
