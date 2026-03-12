/*
 * Copyright 2025 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.configuration.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class DynamicGExtAccountBlockConfiguration {

  @JsonProperty
  private List<String> gextBlockedPhoneNumbers = Collections.emptyList();

  @JsonProperty
  private List<UUID> gextBlockedAccountUuids = Collections.emptyList();

  @JsonProperty
  private boolean gextBlockEnabled = false;

  public List<String> getGextBlockedPhoneNumbers() {
    return gextBlockedPhoneNumbers;
  }

  public List<UUID> getGextBlockedAccountUuids() {
    return gextBlockedAccountUuids;
  }

  public boolean isGextBlockEnabled() {
    return gextBlockEnabled;
  }
}
