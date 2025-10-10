/*
 * Copyright 2013-2021 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm.auth;

import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.whispersystems.textsecuregcm.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicAuthorizationHeader {

  private static final Logger logger = LoggerFactory.getLogger(BasicAuthorizationHeader.class);

  private final String username;
  private final byte deviceId;
  private final String password;

  private BasicAuthorizationHeader(final String username, final byte deviceId, final String password) {
    this.username = username;
    this.deviceId = deviceId;
    this.password = password;
  }

  public static BasicAuthorizationHeader fromString(final String header) throws InvalidAuthorizationHeaderException {
    try {
      logger.debug("Parsing authorization header, length: {}", header != null ? header.length() : 0);

      if (StringUtils.isBlank(header)) {
        logger.warn("Authorization header is blank");
        throw new InvalidAuthorizationHeaderException("Blank header");
      }

      final int spaceIndex = header.indexOf(' ');

      if (spaceIndex == -1) {
        logger.warn("Invalid authorization header format: no space separator found");
        throw new InvalidAuthorizationHeaderException("Invalid authorization header: " + header);
      }

      final String authorizationType = header.substring(0, spaceIndex);
      logger.debug("Authorization type: {}", authorizationType);

      if (!"Basic".equals(authorizationType)) {
        logger.warn("Unsupported authorization method: {}", authorizationType);
        throw new InvalidAuthorizationHeaderException("Unsupported authorization method: " + authorizationType);
      }

      final String credentials;

      try {
        String encodedCredentials = header.substring(spaceIndex + 1);
        logger.debug("Encoded credentials length: {}", encodedCredentials.length());
        credentials = new String(Base64.getDecoder().decode(encodedCredentials));
        logger.debug("Decoded credentials length: {}", credentials.length());
      } catch (final IndexOutOfBoundsException e) {
        logger.warn("Missing credentials in authorization header");
        throw new InvalidAuthorizationHeaderException("Missing credentials");
      } catch (final IllegalArgumentException e) {
        logger.warn("Invalid Base64 encoding in authorization header");
        throw new InvalidAuthorizationHeaderException("Invalid Base64 encoding");
      }

      if (StringUtils.isEmpty(credentials)) {
        logger.warn("Decoded credentials are empty");
        throw new InvalidAuthorizationHeaderException("Bad decoded value: " + credentials);
      }

      final int credentialSeparatorIndex = credentials.indexOf(':');

      if (credentialSeparatorIndex == -1) {
        logger.warn("Credentials missing colon separator");
        throw new InvalidAuthorizationHeaderException("Badly-formatted credentials: " + credentials);
      }

      final String usernameComponent = credentials.substring(0, credentialSeparatorIndex);
      logger.debug("Username component: {}", usernameComponent);

      final String username;
      final byte deviceId;
      {
        final Pair<String, Byte> identifierAndDeviceId =
            AccountAuthenticator.getIdentifierAndDeviceId(usernameComponent);

        username = identifierAndDeviceId.first();
        deviceId = identifierAndDeviceId.second();

        logger.debug("Parsed username: {}, deviceId: {}", username, deviceId);
      }

      final String password = credentials.substring(credentialSeparatorIndex + 1);
      logger.debug("Password length: {}", password != null ? password.length() : 0);

      if (StringUtils.isAnyBlank(username, password)) {
        logger.warn("Username or password are blank - username blank: {}, password blank: {}",
            StringUtils.isBlank(username), StringUtils.isBlank(password));
        throw new InvalidAuthorizationHeaderException("Username or password were blank");
      }

      logger.debug("Successfully parsed authorization header for username: {}", username);
      return new BasicAuthorizationHeader(username, deviceId, password);
    } catch (final IllegalArgumentException | IndexOutOfBoundsException e) {
      logger.warn("Exception parsing authorization header: {}", e.getMessage());
      throw new InvalidAuthorizationHeaderException(e);
    }
  }

  public String getUsername() {
    return username;
  }

  public long getDeviceId() {
    return deviceId;
  }

  public String getPassword() {
    return password;
  }
}
