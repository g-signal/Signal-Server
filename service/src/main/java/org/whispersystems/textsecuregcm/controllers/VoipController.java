/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.controllers;

import io.dropwizard.auth.Auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
import org.whispersystems.textsecuregcm.entities.VoipPushRequest;
import org.whispersystems.textsecuregcm.push.NotPushRegisteredException;
import org.whispersystems.textsecuregcm.push.PushNotificationManager;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.AccountsManager;
import org.whispersystems.textsecuregcm.storage.Device;

@Path("/v1/voip")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VoipController {

    private static final Logger logger = LoggerFactory.getLogger(VoipController.class);

    private final AccountsManager accountsManager;
    private final PushNotificationManager pushNotificationManager;

    public VoipController(AccountsManager accountsManager, PushNotificationManager pushNotificationManager) {
        this.accountsManager = accountsManager;
        this.pushNotificationManager = pushNotificationManager;
    }

    @POST
    @Path("/push")
    public Response sendVoipPush(@Auth AuthenticatedDevice auth,
                                @Valid @NotNull VoipPushRequest request) {
        try {
          final Account account = accountsManager.getByAccountIdentifier(auth.accountIdentifier())
              .orElseThrow(() -> new WebApplicationException(Response.Status.UNAUTHORIZED));

            logger.info("Received VOIP push request from device {}, payload: {}",
                auth.deviceId(),
                request.callMessageRelayPayload());

            // Send VOIP push notification using sendVoipNotification method
            pushNotificationManager.sendVoipNotification(
                account,
                auth.deviceId(),
                request.callMessageRelayPayload() // Use the payload ID from NSE
            );

            logger.info("Successfully sent VOIP push for device {} with payload {}",
                auth.deviceId(), request.callMessageRelayPayload());

            return Response.ok("{\"status\": \"success\"}").build();

        } catch (NotPushRegisteredException e) {
            logger.error("Device not registered for push notifications", e);
            return Response.status(Response.Status.BAD_REQUEST)
                .entity("{\"error\": \"Device not registered for push notifications\"}")
                .build();
        } catch (Exception e) {
            logger.error("Failed to send VOIP push", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{\"error\": \"Internal server error\"}")
                .build();
        }
    }
}
