/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import org.whispersystems.textsecuregcm.util.SystemMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.auth.ExternalServiceCredentialsGenerator;
import org.whispersystems.textsecuregcm.configuration.GextTagConfiguration;
import org.whispersystems.textsecuregcm.configuration.SecureValueRecoveryConfiguration;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetBaUserInfoRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetBaUserInfoResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkResultRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkResultResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkedBaUserInfoRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.GetLinkedBaUserInfoResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.RequestLinkRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.RequestLinkResponse;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.SendMessageRequest;
import org.whispersystems.textsecuregcm.ext_tag.linkbapay.SendMessageResponse;
import org.whispersystems.textsecuregcm.http.FaultTolerantHttpClient;
import org.whispersystems.textsecuregcm.util.HttpUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

/**
 * A client for communicating with the external tag service
 */
public class GextTagClient {

    private static final Logger logger = LoggerFactory.getLogger(GextTagClient.class);
    private static final ObjectMapper objectMapper = SystemMapper.jsonMapper();

    private final URI accountTagQueryUri;
    private final URI groupTagQueryUri;

    private final URI linkbapayCreateQrCodeUri;
    private final URI linkbapayGetBaUserInfoUri;
    private final URI linkbapayRequestLinkUri;
    private final URI linkbapayConfirmLinkUri;
    private final URI linkbapayGetLinkResultUri;
    private final URI linkbapayGetLinkedBaUserInfoUri;
    private final URI linkbapaySendMessageUri;

    private final FaultTolerantHttpClient httpClient;

    static final String ACCOUNT_TAG_QUERY_PATH = "/v1/account/tag/query";
    static final String GROUP_TAG_QUERY_PATH = "/v1/group/tag/query";

    static final String LINKBAPAY_CREATE_QRCODE_PATH = "/v1/linkbapay/link/qrcode/create";
    static final String LINKBAPAY_GET_BA_USER_INFO_PATH = "/v1/linkbapay/link/getBaUserInfo";
    static final String LINKBAPAY_REQUEST_LINK_PATH = "/v1/linkbapay/link/requestLink";
    static final String LINKBAPAY_CONFIRM_LINK_PATH = "/v1/linkbapay/link/confirm";
    static final String LINKBAPAY_GET_LINK_RESULT_PATH = "/v1/linkbapay/link/getLinkResult";
    static final String LINKBAPAY_GET_LINKED_BA_USER_INFO_PATH = "/v1/linkbapay/link/getLinkedBaUserInfo";
    static final String LINKBAPAY_SEND_MESSAGE_PATH = "/v1/linkbapay/message/send";

    static final String ACCOUNT_REG_CALLBACK = "/v1/account/reg/callback";
    static final String ACCOUNT_LOGIN_CALLBACK = "/v1/account/login/callback";

    private final URI accountRegCallbackUri;
    private final URI accountLoginCallbackUri;

    public GextTagClient(
            final Executor executor,
            final ScheduledExecutorService retryExecutor,
            final GextTagConfiguration configuration,
            Supplier<List<Integer>> allowedQueryErrorStatusCodes)
            throws CertificateException {
        final URI baseUri = URI.create(configuration.uri());
        this.accountTagQueryUri = baseUri.resolve(ACCOUNT_TAG_QUERY_PATH);
        this.groupTagQueryUri = baseUri.resolve(GROUP_TAG_QUERY_PATH);

        this.linkbapayCreateQrCodeUri = baseUri.resolve(LINKBAPAY_CREATE_QRCODE_PATH);
        this.linkbapayGetBaUserInfoUri = baseUri.resolve(LINKBAPAY_GET_BA_USER_INFO_PATH);
        this.linkbapayRequestLinkUri = baseUri.resolve(LINKBAPAY_REQUEST_LINK_PATH);
        this.linkbapayConfirmLinkUri = baseUri.resolve(LINKBAPAY_CONFIRM_LINK_PATH);
        this.linkbapayGetLinkResultUri = baseUri.resolve(LINKBAPAY_GET_LINK_RESULT_PATH);
        this.linkbapayGetLinkedBaUserInfoUri = baseUri.resolve(LINKBAPAY_GET_LINKED_BA_USER_INFO_PATH);
        this.linkbapaySendMessageUri = baseUri.resolve(LINKBAPAY_SEND_MESSAGE_PATH);

        this.accountRegCallbackUri = baseUri.resolve(ACCOUNT_REG_CALLBACK);
        this.accountLoginCallbackUri = baseUri.resolve(ACCOUNT_LOGIN_CALLBACK);

      FaultTolerantHttpClient.Builder fBuilder = FaultTolerantHttpClient.newBuilder("ext-tag", executor)
          .withCircuitBreaker(configuration.circuitBreakerConfigurationName())
          .withRetry(configuration.retryConfigurationName(), retryExecutor)
          .withVersion(HttpClient.Version.HTTP_1_1)
          .withConnectTimeout(Duration.ofSeconds(10))
          .withRedirect(HttpClient.Redirect.NEVER)
          .withSecurityProtocol(FaultTolerantHttpClient.SECURITY_PROTOCOL_TLS_1_2);

        if (configuration.extTagCaCertificatesEnabled()) {
            fBuilder.withTrustedServerCertificates(configuration.extTagCaCertificates().toArray(new String[0]));
        }
        this.httpClient = fBuilder.build();
    }

    /**
     * Query account tags by user identifier
     */
    public CompletableFuture<List<GextTag>> queryAccountTags(final String userIdentifier) {
        try {
            URI uri = URI.create(accountTagQueryUri.toString() + "?userIdentifier="+ URLEncoder.encode(userIdentifier, "UTF-8"));
            logger.debug("queryAccountTags:" + uri);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                List<GextTag> tags = objectMapper.readValue(response.body(), new TypeReference<List<GextTag>>() {});
                                logger.debug("Successfully retrieved {} tags for account {}", tags.size(), userIdentifier);
                                return tags;
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse account tags response for identifier {}", userIdentifier, e);
                                return null;
                            }
                        }

                        logger.warn("Failed to query account tags for identifier {} with status {} and response body: {}",
                                userIdentifier, response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception occurred while querying account tags for identifier {}", userIdentifier, throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for account tags query for identifier {}", userIdentifier, e);
            return CompletableFuture.completedFuture(null);
        }
    }


    /**
     * Query group tags
     */
    public CompletableFuture<List<GextTag>> queryGroupTags(final String groupIdentifier) {
        try {
            URI uri = URI.create(groupTagQueryUri.toString() + "?groupIdentifier="+ URLEncoder.encode(groupIdentifier, "UTF-8"));
            logger.debug("queryGroupTags:" + uri);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                List<GextTag> tags = objectMapper.readValue(response.body(), new TypeReference<List<GextTag>>() {});
                                logger.debug("Successfully retrieved {} tags for group {}", tags.size(), groupIdentifier);
                                return tags;
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse group tags response for group {}", groupIdentifier, e);
                                return null;
                            }
                        }

                        logger.warn("Failed to query group tags for group {} with status {} and response body: {}",
                                groupIdentifier, response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception occurred while querying group tags for group {}", groupIdentifier, throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for group tags query for group {}", groupIdentifier, e);
            return CompletableFuture.completedFuture(null);
        }
    }



    /**
     * Proxy → V1LinkBapayController.getBaUserInfo (API 2)
     */
    public CompletableFuture<GetBaUserInfoResponse> getBaUserInfo(final GetBaUserInfoRequest body) {
        try {
            final String json = objectMapper.writeValueAsString(body);
            logger.debug("getBaUserInfo:{} body={}", linkbapayGetBaUserInfoUri, json);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(linkbapayGetBaUserInfoUri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                return objectMapper.readValue(response.body(), GetBaUserInfoResponse.class);
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse getBaUserInfo response", e);
                                return null;
                            }
                        }
                        logger.warn("Failed getBaUserInfo status={} body={}", response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on getBaUserInfo", throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for getBaUserInfo", e);
            return CompletableFuture.completedFuture(null);
        }
    }


    /**
     * Proxy → V1LinkBapayController.requestLink (API 3)
     */
    public CompletableFuture<RequestLinkResponse> requestLink(final RequestLinkRequest body) {
        try {
            final String json = objectMapper.writeValueAsString(body);
            logger.debug("requestLink:{} body={}", linkbapayRequestLinkUri, json);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(linkbapayRequestLinkUri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                return objectMapper.readValue(response.body(), RequestLinkResponse.class);
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse requestLink response", e);
                                return null;
                            }
                        }
                        logger.warn("Failed requestLink status={} body={}", response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on requestLink", throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for requestLink", e);
            return CompletableFuture.completedFuture(null);
        }
    }



    /**
     * Proxy → V1LinkBapayController.getLinkResult (API 5)
     */
    public CompletableFuture<GetLinkResultResponse> getLinkResult(final GetLinkResultRequest body) {
        try {
            final String json = objectMapper.writeValueAsString(body);
            logger.debug("getLinkResult:{} body={}", linkbapayGetLinkResultUri, json);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(linkbapayGetLinkResultUri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                return objectMapper.readValue(response.body(), GetLinkResultResponse.class);
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse getLinkResult response", e);
                                return null;
                            }
                        }
                        logger.warn("Failed getLinkResult status={} body={}", response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on getLinkResult", throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for getLinkResult", e);
            return CompletableFuture.completedFuture(null);
        }
    }


    /**
     * Proxy → V1LinkBapayController.getLinkedBaUserInfo
     * 查询当前 BAXS App 用户在本地 t_signal_user 表中已绑定的 BA 客服信息。失败返回 null。
     */
    public CompletableFuture<GetLinkedBaUserInfoResponse> getLinkedBaUserInfo(final GetLinkedBaUserInfoRequest body) {
        try {
            final String json = objectMapper.writeValueAsString(body);
            logger.debug("getLinkedBaUserInfo:{} body={}", linkbapayGetLinkedBaUserInfoUri, json);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(linkbapayGetLinkedBaUserInfoUri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                return objectMapper.readValue(response.body(), GetLinkedBaUserInfoResponse.class);
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse getLinkedBaUserInfo response", e);
                                return null;
                            }
                        }
                        logger.warn("Failed getLinkedBaUserInfo status={} body={}", response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on getLinkedBaUserInfo", throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for getLinkedBaUserInfo", e);
            return CompletableFuture.completedFuture(null);
        }
    }


    /**
     * Proxy → V1LinkBapayController.sendMessage (API 6)
     */
    public CompletableFuture<SendMessageResponse> sendMessage(final SendMessageRequest body) {
        try {
            final String json = objectMapper.writeValueAsString(body);
            logger.debug("sendMessage:{} body={}", linkbapaySendMessageUri, json);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(linkbapaySendMessageUri)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .timeout(Duration.ofSeconds(30))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .build();

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (HttpUtils.isSuccessfulResponse(response.statusCode())) {
                            try {
                                return objectMapper.readValue(response.body(), SendMessageResponse.class);
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse sendMessage response", e);
                                return null;
                            }
                        }
                        logger.warn("Failed sendMessage status={} body={}", response.statusCode(), response.body());
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on sendMessage", throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for sendMessage", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 用户注册回调
     * @param phoneNumber 用户手机号
     * @param uuid 用户 UUID
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> accountRegCallback(final String phoneNumber, final String uuid) {
        try {
            final Map<String, Object> requestBody = Map.of(
                    "phoneNumber", phoneNumber,
                    "uuid", uuid,
                    "timestamp", System.currentTimeMillis()
            );

            final String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(accountRegCallbackUri)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            logger.info("Sending account registration callback: phoneNumber={}, uuid={}", phoneNumber, uuid);

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            logger.info("Account registration callback successful: phoneNumber={}", phoneNumber);
                        } else {
                            logger.warn("Account registration callback failed: status={}, body={}",
                                    response.statusCode(), response.body());
                        }
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on account registration callback: phoneNumber={}", phoneNumber, throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for account registration callback", e);
            return CompletableFuture.completedFuture(null);
        }
    }

    /**
     * 用户登录回调
     * @param phoneNumber 用户手机号
     * @param uuid 用户 UUID
     * @return CompletableFuture<Void>
     */
    public CompletableFuture<Void> accountLoginCallback(final String phoneNumber, final String uuid) {
        try {
            final Map<String, Object> requestBody = Map.of(
                    "phoneNumber", phoneNumber,
                    "uuid", uuid,
                    "timestamp", System.currentTimeMillis()
            );

            final String requestBodyJson = objectMapper.writeValueAsString(requestBody);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(accountLoginCallbackUri)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .build();

            logger.info("Sending account login callback: phoneNumber={}, uuid={}", phoneNumber, uuid);

            return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(response -> {
                        if (response.statusCode() == 200) {
                            logger.info("Account login callback successful: phoneNumber={}", phoneNumber);
                        } else {
                            logger.warn("Account login callback failed: status={}, body={}",
                                    response.statusCode(), response.body());
                        }
                        return null;
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception on account login callback: phoneNumber={}", phoneNumber, throwable);
                        return null;
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for account login callback", e);
            return CompletableFuture.completedFuture(null);
        }
    }


}
