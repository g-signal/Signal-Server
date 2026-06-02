/*
 * Copyright 2013-2021 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.signal.libsignal.protocol.IdentityKey;
import org.whispersystems.textsecuregcm.ext_robot.GextRobot;
import org.whispersystems.textsecuregcm.identity.ServiceIdentifier;
import org.whispersystems.textsecuregcm.util.ByteArrayBase64WithPaddingAdapter;
import org.whispersystems.textsecuregcm.util.ServiceIdentifierAdapter;
import org.whispersystems.textsecuregcm.util.IdentityKeyAdapter;
import org.whispersystems.textsecuregcm.ext_tag.GextTag;

import java.util.List;
import java.util.Map;

public class BaseProfileResponse {

  @JsonProperty
  @JsonSerialize(using = IdentityKeyAdapter.Serializer.class)
  @JsonDeserialize(using = IdentityKeyAdapter.Deserializer.class)
  private IdentityKey identityKey;

  @JsonProperty
  @JsonSerialize(using = ByteArrayBase64WithPaddingAdapter.Serializing.class)
  @JsonDeserialize(using = ByteArrayBase64WithPaddingAdapter.Deserializing.class)
  private byte[] unidentifiedAccess;

  @JsonProperty
  private boolean unrestrictedUnidentifiedAccess;

  @JsonProperty
  private Map<String, Boolean> capabilities;

  @JsonProperty
  private List<Badge> badges;

  @JsonProperty
  private List<GextTag> gextTags;

  @JsonProperty
  private GextRobot gextRobot;

  @JsonProperty
  @JsonSerialize(using = ServiceIdentifierAdapter.ServiceIdentifierSerializer.class)
  @JsonDeserialize(using = ServiceIdentifierAdapter.ServiceIdentifierDeserializer.class)
  private ServiceIdentifier uuid;

  public BaseProfileResponse() {
  }

  public BaseProfileResponse(final IdentityKey identityKey,
      final byte[] unidentifiedAccess,
      final boolean unrestrictedUnidentifiedAccess,
      final Map<String, Boolean> capabilities,
      final List<Badge> badges,
      final List<GextTag> gextTags,
      final GextRobot gextRobot,
      final ServiceIdentifier uuid) {

    this.identityKey = identityKey;
    this.unidentifiedAccess = unidentifiedAccess;
    this.unrestrictedUnidentifiedAccess = unrestrictedUnidentifiedAccess;
    this.capabilities = capabilities;
    this.badges = badges;
    this.gextTags = gextTags;
    this.gextRobot = gextRobot;
    this.uuid = uuid;
  }

  public IdentityKey getIdentityKey() {
    return identityKey;
  }

  public byte[] getUnidentifiedAccess() {
    return unidentifiedAccess;
  }

  public boolean isUnrestrictedUnidentifiedAccess() {
    return unrestrictedUnidentifiedAccess;
  }

  public Map<String, Boolean> getCapabilities() {
    return capabilities;
  }

  public List<Badge> getBadges() {
    return badges;
  }

  public List<GextTag> getGextTags() {
    return gextTags;
  }

  public GextRobot getGextRobot() {
    return gextRobot;
  }

  public ServiceIdentifier getUuid() {
    return uuid;
  }
}
