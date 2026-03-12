/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.controllers;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.auth.Auth;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.whispersystems.textsecuregcm.auth.AuthenticatedDevice;
import org.whispersystems.textsecuregcm.entities.GExtGroupProfile;
import org.whispersystems.textsecuregcm.ext_tag.ExtTagClient;
import org.whispersystems.textsecuregcm.util.ProfileHelper;

import java.util.List;

@Path("/v1/gext/group/profile")
@Tag(name = "GExtGroupProfile")
public class GExtGroupProfileController {

  private final ExtTagClient extTagClient;

  public GExtGroupProfileController(final ExtTagClient extTagClient) {
    this.extTagClient = extTagClient;
  }

  @Timed
  @GET
  @Path("/{groupId}")
  @Produces(MediaType.APPLICATION_JSON)
  @Operation(
      summary = "Get group profile",
      description = "Retrieve group profile (including tags) by its GroupIdentifier (32-byte hex string)"
  )
  @ApiResponse(responseCode = "200", description = "Successfully retrieved group profile", useReturnTypeSchema = true)
  @ApiResponse(responseCode = "401", description = "Account authentication check failed")
  public GExtGroupProfile getGroupProfile(
      @Auth final AuthenticatedDevice auth,
      @Parameter(description = "The group identifier (32-byte hex string)")
      @PathParam("groupId") final String groupId) {

    return new GExtGroupProfile(ProfileHelper.queryGroupTags(extTagClient, groupId));

  }
}
