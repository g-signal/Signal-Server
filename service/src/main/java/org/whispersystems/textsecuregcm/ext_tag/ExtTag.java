/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.Objects;

/**
 * Represents an external tag with associated metadata
 */
public class ExtTag {

    private final String id;
    private final int type;
    private final String category;
    private final String text;
    private final String description;
    private final String cssColor;
    private final Instant expiration;
    private final boolean visible;
    private final int priority;
    private final String value;

    @JsonCreator
    public ExtTag(
            @JsonProperty("id") final String id,
            @JsonProperty("type") final int type,
            @JsonProperty("category") final String category,
            @JsonProperty("text") final String text,
            @JsonProperty("description") final String description,
            @JsonProperty("cssColor") final String cssColor,
            @JsonProperty("expiration") final Instant expiration,
            @JsonProperty("visible") final boolean visible,
            @JsonProperty("priority") final int priority,
            @JsonProperty("value") final String value) {
        this.id = Objects.requireNonNull(id);
        this.type = type;
        this.category = category;
        this.text = text;
        this.description = description;
        this.cssColor = cssColor;
        this.expiration = expiration;
        this.visible = visible;
        this.priority = priority;
        this.value = value;
    }

    // Constructor for backward compatibility
    public ExtTag(int type, String text, String cssColor) {
        this(generateId(type, text), type, null, text, null, cssColor, null, true, 0, null);
    }

    private static String generateId(int type, String text) {
        return "tag_" + type + "_" + (text != null ? text.toLowerCase().replaceAll("[^a-z0-9]", "_") : "unknown");
    }

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public String getCssColor() {
        return cssColor;
    }

    public Instant getExpiration() {
        return expiration;
    }

    public boolean isVisible() {
        return visible;
    }

    public int getPriority() {
        return priority;
    }

    public String getValue() {
        return value;
    }

    public boolean isExpired() {
        return expiration != null && Instant.now().isAfter(expiration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtTag extTag = (ExtTag) o;
        return type == extTag.type &&
                visible == extTag.visible &&
                priority == extTag.priority &&
                Objects.equals(id, extTag.id) &&
                Objects.equals(category, extTag.category) &&
                Objects.equals(text, extTag.text) &&
                Objects.equals(description, extTag.description) &&
                Objects.equals(cssColor, extTag.cssColor) &&
                Objects.equals(expiration, extTag.expiration) &&
                Objects.equals(value, extTag.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, category, text, description, cssColor, expiration, visible, priority, value);
    }

    @Override
    public String toString() {
        return "ExtTag{" +
                "id='" + id + '\'' +
                ", type=" + type +
                ", category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", description='" + description + '\'' +
                ", cssColor='" + cssColor + '\'' +
                ", expiration=" + expiration +
                ", visible=" + visible +
                ", priority=" + priority +
                ", value='" + value + '\'' +
                '}';
    }

    /**
     * Builder class for ExtTag
     */
    public static class Builder {
        private String id;
        private int type;
        private String category;
        private String text;
        private String description;
        private String cssColor;
        private Instant expiration;
        private boolean visible = true;
        private int priority = 0;
        private String value;

        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        public Builder setType(int type) {
            this.type = type;
            return this;
        }

        public Builder setCategory(String category) {
            this.category = category;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setCssColor(String cssColor) {
            this.cssColor = cssColor;
            return this;
        }

        public Builder setExpiration(Instant expiration) {
            this.expiration = expiration;
            return this;
        }

        public Builder setVisible(boolean visible) {
            this.visible = visible;
            return this;
        }

        public Builder setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public ExtTag build() {
            if (id == null && text != null) {
                id = generateId(type, text);
            }
            return new ExtTag(id, type, category, text, description, cssColor, expiration, visible, priority, value);
        }
    }
}
