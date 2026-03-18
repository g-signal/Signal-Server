package org.whispersystems.textsecuregcm.configuration.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DynamicGExtAccountBlockConfiguration {

  @JsonProperty
  private List<String> blockedPhoneNumbers = Collections.emptyList();

  @JsonProperty
  private List<UUID> blockedAccountUuids = Collections.emptyList();

  @JsonProperty
  private boolean enabled = false;

  private Set<String> phoneNumbersSet;
  private Set<UUID> uuidsSet;

  public Set<String> getBlockedPhoneNumbers() {
    if (phoneNumbersSet == null) {
      phoneNumbersSet = Collections.unmodifiableSet(new HashSet<>(blockedPhoneNumbers));
    }
    return phoneNumbersSet;
  }

  public Set<UUID> getBlockedAccountUuids() {
    if (uuidsSet == null) {
      uuidsSet = Collections.unmodifiableSet(new HashSet<>(blockedAccountUuids));
    }
    return uuidsSet;
  }

  public boolean isEnabled() {
    return enabled;
  }
}
