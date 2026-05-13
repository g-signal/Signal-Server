package org.whispersystems.textsecuregcm.configuration.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

public class DynamicGExtRobotConfiguration {
  @JsonProperty
  private List<UUID> accountUuids = Collections.emptyList();

  @JsonProperty
  private DynamicGExtRobotMsgButtonVisibleConfiguration msgButtonVisible;

  public List<UUID> getAccountUuids() {
    return accountUuids;
  }

  public void setAccountUuids(List<UUID> accountUuids) {
    this.accountUuids = accountUuids;
  }

  public DynamicGExtRobotMsgButtonVisibleConfiguration getMsgButtonVisible() {
    return msgButtonVisible;
  }

  public void setMsgButtonVisible(DynamicGExtRobotMsgButtonVisibleConfiguration msgButtonVisible) {
    this.msgButtonVisible = msgButtonVisible;
  }
}
