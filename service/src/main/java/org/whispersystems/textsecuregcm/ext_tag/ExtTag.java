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

    private String tagId;
    private Integer tagType;
    private String text;
    private String imgBase64;
    private String cssBackgroundColor;
    private String cssColor;
    private Float cssOpacity;
    private Integer cssBorderWidth;
    private Integer cssBorderRadius;
    private String cssBorderColor;
    private String cssBorderStyle;



    @JsonCreator
    public ExtTag(
            @JsonProperty("tagId") final String tagId,
            @JsonProperty("tagType") final Integer tagType,
            @JsonProperty("text") final String text,
            @JsonProperty("imgBase64") final String imgBase64,
            @JsonProperty("cssBackgroundColor") final String cssBackgroundColor,
            @JsonProperty("cssColor") final String cssColor,
            @JsonProperty("cssOpacity") final Float cssOpacity,
            @JsonProperty("cssBorderWidth") final Integer cssBorderWidth,
            @JsonProperty("cssBorderRadius") final Integer cssBorderRadius,
            @JsonProperty("cssBorderColor") final String cssBorderColor,
            @JsonProperty("cssBorderStyle") final String cssBorderStyle) {
        this.tagId = tagId;
        this.tagType = tagType;
        this.text = text;
        this.imgBase64 = imgBase64;
        this.cssBackgroundColor = cssBackgroundColor;
        this.cssColor = cssColor;
        this.cssOpacity = cssOpacity;
        this.cssBorderWidth = cssBorderWidth;
        this.cssBorderRadius = cssBorderRadius;
        this.cssBorderColor = cssBorderColor;
        this.cssBorderStyle = cssBorderStyle;
    }

    // Constructor for backward compatibility
    public ExtTag(Integer tagType, String text, String cssColor) {
        this(null, tagType, text, null, null, cssColor, null, null, null, null, null);
    }

    public String getTagId() {
        return tagId;
    }

    public Integer getTagType() {
        return tagType;
    }

    public String getText() {
        return text;
    }

    public String getCssColor() {
        return cssColor;
    }
    public String getImgBase64() {
        return imgBase64;
    }

    public String getCssBackgroundColor() {
        return cssBackgroundColor;
    }

    public Float getCssOpacity() {
        return cssOpacity;
    }

    public Integer getCssBorderWidth() {
        return cssBorderWidth;
    }

    public Integer getCssBorderRadius() {
        return cssBorderRadius;
    }

    public String getCssBorderColor() {
        return cssBorderColor;
    }

    public String getCssBorderStyle() {
        return cssBorderStyle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtTag extTag = (ExtTag) o;
        return Objects.equals(tagId, extTag.tagId) &&
                Objects.equals(tagType, extTag.tagType) &&
                Objects.equals(text, extTag.text) &&
                Objects.equals(imgBase64, extTag.imgBase64) &&
                Objects.equals(cssBackgroundColor, extTag.cssBackgroundColor) &&
                Objects.equals(cssColor, extTag.cssColor) &&
                Objects.equals(cssOpacity, extTag.cssOpacity) &&
                Objects.equals(cssBorderWidth, extTag.cssBorderWidth) &&
                Objects.equals(cssBorderRadius, extTag.cssBorderRadius) &&
                Objects.equals(cssBorderColor, extTag.cssBorderColor) &&
                Objects.equals(cssBorderStyle, extTag.cssBorderStyle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tagId, tagType, text, imgBase64, cssBackgroundColor, cssColor, cssOpacity, cssBorderWidth, cssBorderRadius, cssBorderColor, cssBorderStyle);
    }

    @Override
    public String toString() {
        return "ExtTag{" +
                "tagId=" + tagId +
                ", tagType=" + tagType +
                ", text='" + text + '\'' +
                ", imgBase64='" + imgBase64 + '\'' +
                ", cssBackgroundColor='" + cssBackgroundColor + '\'' +
                ", cssColor='" + cssColor + '\'' +
                ", cssOpacity=" + cssOpacity +
                ", cssBorderWidth=" + cssBorderWidth +
                ", cssBorderRadius=" + cssBorderRadius +
                ", cssBorderColor='" + cssBorderColor + '\'' +
                ", cssBorderStyle='" + cssBorderStyle + '\'' +
                '}';
    }

    /**
     * Builder class for ExtTag
     */
    public static class Builder {
        private String tagId;
        private Integer tagType;
        private String text;
        private String imgBase64;
        private String cssBackgroundColor;
        private String cssColor;
        private Float cssOpacity;
        private Integer cssBorderWidth;
        private Integer cssBorderRadius;
        private String cssBorderColor;
        private String cssBorderStyle;

        public Builder setTagId(String tagId) {
            this.tagId = tagId;
            return this;
        }

        public Builder setTagType(Integer tagType) {
            this.tagType = tagType;
            return this;
        }

        public Builder setText(String text) {
            this.text = text;
            return this;
        }

        public Builder setImgBase64(String imgBase64) {
            this.imgBase64 = imgBase64;
            return this;
        }

        public Builder setCssBackgroundColor(String cssBackgroundColor) {
            this.cssBackgroundColor = cssBackgroundColor;
            return this;
        }

        public Builder setCssColor(String cssColor) {
            this.cssColor = cssColor;
            return this;
        }

        public Builder setCssOpacity(Float cssOpacity) {
            this.cssOpacity = cssOpacity;
            return this;
        }

        public Builder setCssBorderWidth(Integer cssBorderWidth) {
            this.cssBorderWidth = cssBorderWidth;
            return this;
        }

        public Builder setCssBorderRadius(Integer cssBorderRadius) {
            this.cssBorderRadius = cssBorderRadius;
            return this;
        }

        public Builder setCssBorderColor(String cssBorderColor) {
            this.cssBorderColor = cssBorderColor;
            return this;
        }

        public Builder setCssBorderStyle(String cssBorderStyle) {
            this.cssBorderStyle = cssBorderStyle;
            return this;
        }

        public ExtTag build() {
            return new ExtTag(tagId, tagType, text, imgBase64, cssBackgroundColor, cssColor, cssOpacity, cssBorderWidth, cssBorderRadius, cssBorderColor, cssBorderStyle);
        }
    }
}
