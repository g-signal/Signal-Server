package org.whispersystems.textsecuregcm.configuration.dynamic;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DynamicGExtRobotMsgButtonVisibleConfiguration {
  @JsonProperty
  private boolean text;//文本输入框，肯定会显示，忽略这个判断
  @JsonProperty
  private boolean sticker;
  @JsonProperty
  private boolean camera;
  @JsonProperty
  private boolean microphone;
  @JsonProperty
  private boolean photos;
  @JsonProperty
  private boolean gif;
  @JsonProperty
  private boolean file;
  @JsonProperty
  private boolean contact;
  @JsonProperty

  private boolean location;
  @JsonProperty
  private boolean payment;
  @JsonProperty
  private boolean poll;
  public boolean isText() {
    return text;
  }

  public void setText(boolean text) {
    this.text = text;
  }

  public boolean isSticker() {
    return sticker;
  }

  public void setSticker(boolean sticker) {
    this.sticker = sticker;
  }

  public boolean isCamera() {
    return camera;
  }

  public void setCamera(boolean camera) {
    this.camera = camera;
  }

  public boolean isMicrophone() {
    return microphone;
  }

  public void setMicrophone(boolean microphone) {
    this.microphone = microphone;
  }

  public boolean isPhotos() {
    return photos;
  }

  public void setPhotos(boolean photos) {
    this.photos = photos;
  }

  public boolean isGif() {
    return gif;
  }

  public void setGif(boolean gif) {
    this.gif = gif;
  }

  public boolean isFile() {
    return file;
  }

  public void setFile(boolean file) {
    this.file = file;
  }

  public boolean isContact() {
    return contact;
  }

  public void setContact(boolean contact) {
    this.contact = contact;
  }

  public boolean isLocation() {
    return location;
  }

  public void setLocation(boolean location) {
    this.location = location;
  }

  public boolean isPayment() {
    return payment;
  }

  public void setPayment(boolean payment) {
    this.payment = payment;
  }

  public boolean isPoll() {
    return poll;
  }

  public void setPoll(boolean poll) {
    this.poll = poll;
  }
}
