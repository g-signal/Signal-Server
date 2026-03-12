/*
 * Copyright 2023 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package org.whispersystems.textsecuregcm.ext_tag;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.whispersystems.textsecuregcm.configuration.ExtTagConfiguration;
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
public class ExtTagClient {

    private static final Logger logger = LoggerFactory.getLogger(ExtTagClient.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final URI accountTagQueryUri;
    private final URI groupTagQueryUri;

    private final FaultTolerantHttpClient httpClient;

    static final String ACCOUNT_TAG_QUERY_PATH = "/v1/account/tag/query";
    static final String GROUP_TAG_QUERY_PATH = "/v1/group/tag/query";

    public ExtTagClient(
            final Executor executor,
            final ScheduledExecutorService retryExecutor,
            final ExtTagConfiguration configuration,
            Supplier<List<Integer>> allowedQueryErrorStatusCodes)
            throws CertificateException {

        final URI baseUri = URI.create(configuration.uri());
        this.accountTagQueryUri = baseUri.resolve(ACCOUNT_TAG_QUERY_PATH);
        this.groupTagQueryUri = baseUri.resolve(GROUP_TAG_QUERY_PATH);

        FaultTolerantHttpClient.Builder fBuilder = FaultTolerantHttpClient.newBuilder()
                .withCircuitBreaker(configuration.circuitBreaker())
                .withRetry(configuration.retry())
                .withRetryExecutor(retryExecutor)
                .withVersion(HttpClient.Version.HTTP_1_1)
                .withConnectTimeout(Duration.ofSeconds(10))
                .withRedirect(HttpClient.Redirect.NEVER)
                .withExecutor(executor)
                .withName("ext-tag")
                .withSecurityProtocol(FaultTolerantHttpClient.SECURITY_PROTOCOL_TLS_1_2);

        if (configuration.extTagCaCertificatesEnabled()) {
            fBuilder.withTrustedServerCertificates(configuration.extTagCaCertificates().toArray(new String[0]));
        }
        this.httpClient = fBuilder.build();
    }

    /**
     * Query account tags by UUID
     */
    public CompletableFuture<List<ExtTag>> queryAccountTags(final UUID accountUuid) {
        return queryAccountTags(accountUuid.toString());
    }

    /**
     * Query account tags by user identifier
     */
    public CompletableFuture<List<ExtTag>> queryAccountTags(final String userIdentifier) {
        try {
            URI uri = URI.create(accountTagQueryUri.toString() + "?userIdentifier="+ URLEncoder.encode(userIdentifier, "UTF-8"));

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
                                List<ExtTag> tags = objectMapper.readValue(response.body(), new TypeReference<List<ExtTag>>() {});
                                logger.debug("Successfully retrieved {} tags for account {}", tags.size(), userIdentifier);
                                return tags;
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse account tags response for identifier {}", userIdentifier, e);
                                return Collections.<ExtTag>emptyList();
                            }
                        }

                        logger.warn("Failed to query account tags for identifier {} with status {} and response body: {}",
                                userIdentifier, response.statusCode(), response.body());
                        return Collections.<ExtTag>emptyList();
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception occurred while querying account tags for identifier {}", userIdentifier, throwable);
                        return Collections.<ExtTag>emptyList();
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for account tags query for identifier {}", userIdentifier, e);
            return CompletableFuture.completedFuture(Collections.<ExtTag>emptyList());
        }
    }


    /**
     * Query group tags
     */
    public CompletableFuture<List<ExtTag>> queryGroupTags(final String groupIdentifier) {
        try {
            URI uri = URI.create(groupTagQueryUri.toString() + "?groupIdentifier="+ URLEncoder.encode(groupIdentifier, "UTF-8"));

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
                                List<ExtTag> tags = objectMapper.readValue(response.body(), new TypeReference<List<ExtTag>>() {});
                                logger.debug("Successfully retrieved {} tags for group {}", tags.size(), groupIdentifier);
                                return tags;
                            } catch (JsonProcessingException e) {
                                logger.error("Failed to parse group tags response for group {}", groupIdentifier, e);
                                return Collections.<ExtTag>emptyList();
                            }
                        }

                        logger.warn("Failed to query group tags for group {} with status {} and response body: {}",
                                groupIdentifier, response.statusCode(), response.body());
                        return Collections.<ExtTag>emptyList();
                    })
                    .exceptionally(throwable -> {
                        logger.error("Exception occurred while querying group tags for group {}", groupIdentifier, throwable);
                        return Collections.<ExtTag>emptyList();
                    });
        } catch (Exception e) {
            logger.error("Failed to create request for group tags query for group {}", groupIdentifier, e);
            return CompletableFuture.completedFuture(Collections.<ExtTag>emptyList());
        }
    }


}
