/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.grpc;

import io.grpc.Status;
import org.signal.chat.gext.GetGroupProfileRequest;
import org.signal.chat.gext.GetGroupProfileResponse;
import org.signal.chat.gext.ReactorGExtGroupProfileGrpc;
import org.whispersystems.textsecuregcm.ext_tag.ExtTagClient;
import org.whispersystems.textsecuregcm.util.ProfileHelper;
import reactor.core.publisher.Mono;

import java.util.List;

public class GExtGroupProfileGrpcService extends ReactorGExtGroupProfileGrpc.GExtGroupProfileImplBase {

  private final ExtTagClient extTagClient;

  public GExtGroupProfileGrpcService(final ExtTagClient extTagClient) {
    this.extTagClient = extTagClient;
  }

  @Override
  public Mono<GetGroupProfileResponse> getGroupProfile(final GetGroupProfileRequest request) {
    return Mono.fromCallable(() -> {
      final String groupId = request.getGroupId();

      if (groupId == null || groupId.isEmpty()) {
        throw Status.INVALID_ARGUMENT
            .withDescription("Group identifier must not be empty")
            .asRuntimeException();
      }

      final List<org.whispersystems.textsecuregcm.ext_tag.ExtTag> extTags =
          ProfileHelper.queryGroupTags(extTagClient, groupId);

      final GetGroupProfileResponse.Builder responseBuilder = GetGroupProfileResponse.newBuilder();

      for (org.whispersystems.textsecuregcm.ext_tag.ExtTag tag : extTags) {
        org.signal.chat.profile.ExtTag.Builder tagBuilder = org.signal.chat.profile.ExtTag.newBuilder();

        if (tag.getTagId() != null) {
          tagBuilder.setTagId(tag.getTagId());
        }
        if (tag.getTagType() != null) {
          tagBuilder.setTagType(tag.getTagType());
        }
        if (tag.getText() != null) {
          tagBuilder.setText(tag.getText());
        }
        if (tag.getImgBase64() != null) {
          tagBuilder.setImgBase64(tag.getImgBase64());
        }
        if (tag.getCssBackgroundColor() != null) {
          tagBuilder.setCssBackgroundColor(tag.getCssBackgroundColor());
        }
        if (tag.getCssColor() != null) {
          tagBuilder.setCssColor(tag.getCssColor());
        }
        if (tag.getCssOpacity() != null) {
          tagBuilder.setCssOpacity(tag.getCssOpacity());
        }
        if (tag.getCssBorderWidth() != null) {
          tagBuilder.setCssBorderWidth(tag.getCssBorderWidth());
        }
        if (tag.getCssBorderRadius() != null) {
          tagBuilder.setCssBorderRadius(tag.getCssBorderRadius());
        }
        if (tag.getCssBorderColor() != null) {
          tagBuilder.setCssBorderColor(tag.getCssBorderColor());
        }
        if (tag.getCssBorderStyle() != null) {
          tagBuilder.setCssBorderStyle(tag.getCssBorderStyle());
        }

        responseBuilder.addExtTags(tagBuilder.build());
      }

      return responseBuilder.build();
    });
  }
}
