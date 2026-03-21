/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.grpc;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import io.grpc.Status;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import io.grpc.StatusException;
import org.signal.chat.profile.Badge;
import org.signal.chat.profile.BadgeSvg;
import org.signal.chat.profile.GextTag;
import org.signal.chat.profile.GetExpiringProfileKeyCredentialResponse;
import org.signal.chat.profile.GetUnversionedProfileResponse;
import org.signal.chat.profile.GetVersionedProfileResponse;
import org.signal.libsignal.protocol.ServiceId;
import org.signal.libsignal.zkgroup.InvalidInputException;
import org.signal.libsignal.zkgroup.VerificationFailedException;
import org.signal.libsignal.zkgroup.profiles.ExpiringProfileKeyCredentialResponse;
import org.signal.libsignal.zkgroup.profiles.ServerZkProfileOperations;
import org.whispersystems.textsecuregcm.auth.UnidentifiedAccessChecksum;
import org.whispersystems.textsecuregcm.badges.ProfileBadgeConverter;
import org.whispersystems.textsecuregcm.ext_tag.GextTagClient;
import org.whispersystems.textsecuregcm.identity.ServiceIdentifier;
import org.whispersystems.textsecuregcm.storage.Account;
import org.whispersystems.textsecuregcm.storage.DeviceCapability;
import org.whispersystems.textsecuregcm.storage.ProfilesManager;
import org.whispersystems.textsecuregcm.storage.VersionedProfile;
import org.whispersystems.textsecuregcm.util.ProfileHelper;

public class ProfileGrpcHelper {
  static GetVersionedProfileResponse getVersionedProfile(final Account account,
      final ProfilesManager profilesManager,
      final String requestVersion) throws StatusException {

    final VersionedProfile profile = profilesManager.get(account.getUuid(), requestVersion)
        .orElseThrow(Status.NOT_FOUND.withDescription("Profile version not found")::asException);

    final GetVersionedProfileResponse.Builder responseBuilder = GetVersionedProfileResponse.newBuilder();

    responseBuilder
        .setName(ByteString.copyFrom(profile.name()))
        .setAbout(ByteString.copyFrom(profile.about()))
        .setAboutEmoji(ByteString.copyFrom(profile.aboutEmoji()))
        .setAvatar(profile.avatar())
        .setPhoneNumberSharing(ByteString.copyFrom(profile.phoneNumberSharing()));

    // Allow requests where either the version matches the latest version on Account or the latest version on Account
    // is empty to read the payment address.
    if (account.getCurrentProfileVersion().map(v -> v.equals(requestVersion)).orElse(true)) {
      responseBuilder.setPaymentAddress(ByteString.copyFrom(profile.paymentAddress()));
    }

    return responseBuilder.build();
  }

  @VisibleForTesting
  static List<Badge> buildBadges(final List<org.whispersystems.textsecuregcm.entities.Badge> badges) {
    final ArrayList<Badge> grpcBadges = new ArrayList<>();
    for (final org.whispersystems.textsecuregcm.entities.Badge badge : badges) {
      grpcBadges.add(Badge.newBuilder()
          .setId(badge.getId())
          .setCategory(badge.getCategory())
          .setName(badge.getName())
          .setDescription(badge.getDescription())
          .addAllSprites6(badge.getSprites6())
          .setSvg(badge.getSvg())
          .addAllSvgs(buildBadgeSvgs(badge.getSvgs()))
          .build());
    }
    return grpcBadges;
  }

  @VisibleForTesting
  static List<org.signal.chat.common.DeviceCapability> buildAccountCapabilities(final Account account) {
    return Arrays.stream(DeviceCapability.values())
        .filter(DeviceCapability::includeInProfile)
        .filter(account::hasCapability)
        .map(DeviceCapabilityUtil::toGrpcDeviceCapability)
        .toList();
  }

  private static List<BadgeSvg> buildBadgeSvgs(final List<org.whispersystems.textsecuregcm.entities.BadgeSvg> badgeSvgs) {
    ArrayList<BadgeSvg> grpcBadgeSvgs = new ArrayList<>();
    for (final org.whispersystems.textsecuregcm.entities.BadgeSvg badgeSvg : badgeSvgs) {
      grpcBadgeSvgs.add(BadgeSvg.newBuilder()
          .setDark(badgeSvg.getDark())
          .setLight(badgeSvg.getLight())
          .build());
    }
    return grpcBadgeSvgs;
  }

  @VisibleForTesting
  static List<GextTag> buildGextTags(final List<org.whispersystems.textsecuregcm.ext_tag.GextTag> gextTags) {
    final ArrayList<GextTag> grpcGextTags = new ArrayList<>();
    for (final org.whispersystems.textsecuregcm.ext_tag.GextTag gextTag : gextTags) {
      GextTag.Builder builder = GextTag.newBuilder()
          .setText(gextTag.getText() != null ? gextTag.getText() : "");

      if (gextTag.getTagId() != null) {
        builder.setTagId(gextTag.getTagId());
      }

      if (gextTag.getTagType() != null) {
        builder.setTagType(gextTag.getTagType());
      }

      if (gextTag.getImgBase64() != null) {
        builder.setImgBase64(gextTag.getImgBase64());
      }

      if (gextTag.getCssBackgroundColor() != null) {
        builder.setCssBackgroundColor(gextTag.getCssBackgroundColor());
      }

      if (gextTag.getCssColor() != null) {
        builder.setCssColor(gextTag.getCssColor());
      }

      if (gextTag.getCssOpacity() != null) {
        builder.setCssOpacity(gextTag.getCssOpacity());
      }

      if (gextTag.getCssBorderWidth() != null) {
        builder.setCssBorderWidth(gextTag.getCssBorderWidth());
      }

      if (gextTag.getCssBorderRadius() != null) {
        builder.setCssBorderRadius(gextTag.getCssBorderRadius());
      }

      if (gextTag.getCssBorderColor() != null) {
        builder.setCssBorderColor(gextTag.getCssBorderColor());
      }

      if (gextTag.getCssBorderStyle() != null) {
        builder.setCssBorderStyle(gextTag.getCssBorderStyle());
      }

      grpcGextTags.add(builder.build());
    }
    return grpcGextTags;
  }

  static GetUnversionedProfileResponse buildUnversionedProfileResponse(
      final ServiceIdentifier targetIdentifier,
      final UUID requesterUuid,
      final Account targetAccount,
      final ProfileBadgeConverter profileBadgeConverter,
      final GextTagClient extTagClient) {
    final GetUnversionedProfileResponse.Builder responseBuilder = GetUnversionedProfileResponse.newBuilder()
        .setIdentityKey(ByteString.copyFrom(targetAccount.getIdentityKey(targetIdentifier.identityType()).serialize()))
        .addAllCapabilities(buildAccountCapabilities(targetAccount));

    switch (targetIdentifier.identityType()) {
      case ACI -> {
        responseBuilder.setUnrestrictedUnidentifiedAccess(targetAccount.isUnrestrictedUnidentifiedAccess())
            .addAllBadges(buildBadges(profileBadgeConverter.convert(
                RequestAttributesUtil.getAvailableAcceptedLocales(),
                targetAccount.getBadges(),
                ProfileHelper.isSelfProfileRequest(requesterUuid, targetIdentifier))));

        targetAccount.getUnidentifiedAccessKey()
            .map(UnidentifiedAccessChecksum::generateFor)
            .map(ByteString::copyFrom)
            .ifPresent(responseBuilder::setUnidentifiedAccess);

        // Query external tags for this account
        final List<org.whispersystems.textsecuregcm.ext_tag.GextTag> gextTags =
                ProfileHelper.queryExternalTags(extTagClient, targetAccount.getUuid());
        if(gextTags!=null) {
          responseBuilder.addAllGextTags(buildGextTags(gextTags));
        }
      }
      case PNI -> responseBuilder.setUnrestrictedUnidentifiedAccess(false);
    }

    return responseBuilder.build();
  }

  // Overloaded method for backward compatibility - returns empty ext_tags when no ExtTagClient provided
  static GetUnversionedProfileResponse buildUnversionedProfileResponse(
      final ServiceIdentifier targetIdentifier,
      final UUID requesterUuid,
      final Account targetAccount,
      final ProfileBadgeConverter profileBadgeConverter) {
    return buildUnversionedProfileResponse(targetIdentifier, requesterUuid, targetAccount, profileBadgeConverter, null);
  }

  static GetExpiringProfileKeyCredentialResponse getExpiringProfileKeyCredentialResponse(
      final UUID targetUuid,
      final String version,
      final byte[] encodedCredentialRequest,
      final ProfilesManager profilesManager,
      final ServerZkProfileOperations zkProfileOperations) throws StatusException {

    final VersionedProfile profile = profilesManager.get(targetUuid, version)
        .orElseThrow(Status.NOT_FOUND.withDescription("Profile version not found")::asException);

    final ExpiringProfileKeyCredentialResponse profileKeyCredentialResponse;
    try {
      profileKeyCredentialResponse = ProfileHelper.getExpiringProfileKeyCredential(encodedCredentialRequest,
          profile, new ServiceId.Aci(targetUuid), zkProfileOperations);
    } catch (VerificationFailedException | InvalidInputException e) {
      throw Status.INVALID_ARGUMENT.withCause(e).asException();
    }

    return GetExpiringProfileKeyCredentialResponse.newBuilder()
        .setProfileKeyCredential(ByteString.copyFrom(profileKeyCredentialResponse.serialize()))
        .build();
  }
}
