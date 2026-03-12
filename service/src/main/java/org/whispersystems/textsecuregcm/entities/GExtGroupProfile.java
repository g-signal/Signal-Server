/*
 * Copyright 2024 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.whispersystems.textsecuregcm.ext_tag.ExtTag;

import java.util.List;

public record GExtGroupProfile(
    @JsonProperty("extTags")
    List<ExtTag> extTags
) {
}
