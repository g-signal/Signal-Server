/*
 * Copyright 2013 Signal Messenger, LLC
 * SPDX-License-Identifier: AGPL-3.0-only
 */
package org.whispersystems.textsecuregcm;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.whispersystems.textsecuregcm.attachments.TusConfiguration;
import org.whispersystems.textsecuregcm.configuration.*;
import org.whispersystems.textsecuregcm.limits.RateLimiterConfig;
import org.whispersystems.websocket.configuration.WebSocketConfiguration;

/** @noinspection MismatchedQueryAndUpdateOfCollection, WeakerAccess */
public class WhisperServerConfiguration extends Configuration {

  @NotNull
  @Valid
  @JsonProperty
  private TlsKeyStoreConfiguration tlsKeyStore;

  @NotNull
  @Valid
  @JsonProperty
  AwsCredentialsProviderFactory awsCredentialsProvider = new DefaultAwsCredentialsFactory();

  @NotNull
  @Valid
  @JsonProperty
  private StripeConfiguration stripe;

  @NotNull
  @Valid
  @JsonProperty
  private BraintreeConfiguration braintree;

  @NotNull
  @Valid
  @JsonProperty
  private GooglePlayBillingConfiguration googlePlayBilling;

  @NotNull
  @Valid
  @JsonProperty
  private AppleAppStoreConfiguration appleAppStore;

  @NotNull
  @Valid
  @JsonProperty
  private AppleDeviceCheckConfiguration appleDeviceCheck;

  @NotNull
  @Valid
  @JsonProperty
  private DeviceCheckConfiguration deviceCheck;

  @NotNull
  @Valid
  @JsonProperty
  private DynamoDbClientFactory dynamoDbClient;

  @NotNull
  @Valid
  @JsonProperty
  private DynamoDbTables dynamoDbTables;

  @NotNull
  @Valid
  @JsonProperty
  private GcpAttachmentsConfiguration gcpAttachments;

  @NotNull
  @Valid
  @JsonProperty
  private CdnConfiguration cdn;

  @NotNull
  @Valid
  @JsonProperty
  private Cdn3StorageManagerConfiguration cdn3StorageManager;

  @NotNull
  @Valid
  @JsonProperty
  private DatadogConfiguration dogstatsd = new DogstatsdConfiguration();

  @NotNull
  @Valid
  @JsonProperty
  private OpenTelemetryConfiguration openTelemetry;

  @NotNull
  @Valid
  @JsonProperty
  private FaultTolerantRedisClusterFactory cacheCluster;

  @NotNull
  @Valid
  @JsonProperty
  private FaultTolerantRedisClientFactory pubsub;

  @NotNull
  @Valid
  @JsonProperty
  private DirectoryV2Configuration directoryV2;

  @NotNull
  @Valid
  @JsonProperty
  private SecureValueRecoveryConfiguration svr2;

  @NotNull
  @Valid
  @JsonProperty
  private SecureValueRecoveryConfiguration svrb;


  @NotNull
  @Valid
  @JsonProperty
  private GextTagConfiguration gextTag;

  @NotNull
  @Valid
  @JsonProperty
  private FaultTolerantRedisClusterFactory pushSchedulerCluster;

  @NotNull
  @Valid
  @JsonProperty
  private FaultTolerantRedisClusterFactory rateLimitersCluster;

  @NotNull
  @Valid
  @JsonProperty
  private MessageCacheConfiguration messageCache;

  @Valid
  @NotNull
  @JsonProperty
  private List<MaxDeviceConfiguration> maxDevices = new LinkedList<>();

  @Valid
  @NotNull
  @JsonProperty
  private Map<String, RateLimiterConfig> limits = new HashMap<>();

  @Valid
  @NotNull
  @JsonProperty
  private WebSocketConfiguration webSocket = new WebSocketConfiguration();

  @Valid
  @NotNull
  @JsonProperty
  private FcmConfiguration fcm;

  @Valid
  @NotNull
  @JsonProperty
  private ApnConfiguration apn;

  @Valid
  @NotNull
  @JsonProperty
  private UnidentifiedDeliveryConfiguration unidentifiedDelivery;

  @Valid
  @NotNull
  @JsonProperty
  private ShortCodeExpanderConfiguration shortCode;

  @Valid
  @NotNull
  @JsonProperty
  private SecureStorageServiceConfiguration storageService;

  @Valid
  @NotNull
  @JsonProperty
  private PaymentsServiceConfiguration paymentsService;

  @Valid
  @NotNull
  @JsonProperty
  private ZkConfig zkConfig;

  @Valid
  @NotNull
  @JsonProperty
  private GenericZkConfig callingZkConfig;

  @Valid
  @NotNull
  @JsonProperty
  private GenericZkConfig backupsZkConfig;

  @Valid
  @NotNull
  @JsonProperty
  private RemoteConfigConfiguration remoteConfig;

  @Valid
  @NotNull
  @JsonProperty
  private S3ObjectMonitorFactory dynamicConfig;

  @Valid
  @NotNull
  @JsonProperty
  private BadgesConfiguration badges;

  @Valid
  @JsonProperty
  @NotNull
  private SubscriptionConfiguration subscription;

  @Valid
  @JsonProperty
  @NotNull
  private OneTimeDonationConfiguration oneTimeDonations;

  @Valid
  @JsonProperty
  @NotNull
  private PagedSingleUseKEMPreKeyStoreConfiguration pagedSingleUseKEMPreKeyStore;

  @Valid
  @NotNull
  @JsonProperty
  private ReportMessageConfiguration reportMessage = new ReportMessageConfiguration();

  @Valid
  @JsonProperty
  private SpamFilterConfiguration spamFilter;

  @Valid
  @NotNull
  @JsonProperty
  private RegistrationServiceClientFactory registrationService;

  @Valid
  @NotNull
  @JsonProperty
  private TurnConfiguration turn;

  @Valid
  @NotNull
  @JsonProperty
  private TusConfiguration tus;

  @Valid
  @NotNull
  @JsonProperty
  private ClientReleaseConfiguration clientRelease = new ClientReleaseConfiguration(Duration.ofHours(4));

  @Valid
  @NotNull
  @JsonProperty
  private MessageByteLimitCardinalityEstimatorConfiguration messageByteLimitCardinalityEstimator = new MessageByteLimitCardinalityEstimatorConfiguration(Duration.ofDays(1));

  @Valid
  @NotNull
  @JsonProperty
  private LinkDeviceSecretConfiguration linkDevice;

  @Valid
  @NotNull
  @JsonProperty
  private VirtualThreadConfiguration virtualThread = new VirtualThreadConfiguration(Duration.ofMillis(1));

  @Valid
  @NotNull
  @JsonProperty
  private NoiseTunnelConfiguration noiseTunnel;

  @Valid
  @NotNull
  @JsonProperty
  private ExternalRequestFilterConfiguration externalRequestFilter;

  @Valid
  @NotNull
  @JsonProperty
  private KeyTransparencyServiceConfiguration keyTransparencyService;

  @JsonProperty
  private boolean logMessageDeliveryLoops;

  @JsonProperty
  private IdlePrimaryDeviceReminderConfiguration idlePrimaryDeviceReminder =
      new IdlePrimaryDeviceReminderConfiguration(Duration.ofDays(30));

  public TlsKeyStoreConfiguration getTlsKeyStoreConfiguration() {
    return tlsKeyStore;
  }

  public AwsCredentialsProviderFactory getAwsCredentialsConfiguration() {
    return awsCredentialsProvider;
  }

  public StripeConfiguration getStripe() {
    return stripe;
  }

  public BraintreeConfiguration getBraintree() {
    return braintree;
  }

  public GooglePlayBillingConfiguration getGooglePlayBilling() {
    return googlePlayBilling;
  }

  public AppleAppStoreConfiguration getAppleAppStore() {
    return appleAppStore;
  }

  public AppleDeviceCheckConfiguration getAppleDeviceCheck() {
    return appleDeviceCheck;
  }

  public DeviceCheckConfiguration getDeviceCheck() {
    return deviceCheck;
  }

  public DynamoDbClientFactory getDynamoDbClientConfiguration() {
    return dynamoDbClient;
  }

  public DynamoDbTables getDynamoDbTables() {
    return dynamoDbTables;
  }

  public ShortCodeExpanderConfiguration getShortCodeRetrieverConfiguration() {
    return shortCode;
  }

  public WebSocketConfiguration getWebSocketConfiguration() {
    return webSocket;
  }

  public GcpAttachmentsConfiguration getGcpAttachmentsConfiguration() {
    return gcpAttachments;
  }

  public FaultTolerantRedisClusterFactory getCacheClusterConfiguration() {
    return cacheCluster;
  }

  public FaultTolerantRedisClientFactory getRedisPubSubConfiguration() {
    return pubsub;
  }

  public SecureValueRecoveryConfiguration getSvr2Configuration() {
    return svr2;
  }

  public SecureValueRecoveryConfiguration getSvrbConfiguration() {
    return svrb;
  }

  public GextTagConfiguration getGextTag() {
    return gextTag;
  }

  public DirectoryV2Configuration getDirectoryV2Configuration() {
    return directoryV2;
  }



  public SecureStorageServiceConfiguration getSecureStorageServiceConfiguration() {
    return storageService;
  }

  public MessageCacheConfiguration getMessageCacheConfiguration() {
    return messageCache;
  }

  public FaultTolerantRedisClusterFactory getPushSchedulerCluster() {
    return pushSchedulerCluster;
  }

  public FaultTolerantRedisClusterFactory getRateLimitersCluster() {
    return rateLimitersCluster;
  }

  public FcmConfiguration getFcmConfiguration() {
    return fcm;
  }

  public ApnConfiguration getApnConfiguration() {
    return apn;
  }

  public CdnConfiguration getCdnConfiguration() {
    return cdn;
  }

  public Cdn3StorageManagerConfiguration getCdn3StorageManagerConfiguration() {
    return cdn3StorageManager;
  }

  public DatadogConfiguration getDatadogConfiguration() {
    return dogstatsd;
  }

  public OpenTelemetryConfiguration getOpenTelemetryConfiguration() {
    return openTelemetry;
  }

  public UnidentifiedDeliveryConfiguration getDeliveryCertificate() {
    return unidentifiedDelivery;
  }

  public Map<String, Integer> getMaxDevices() {
    Map<String, Integer> results = new HashMap<>();

    for (MaxDeviceConfiguration maxDeviceConfiguration : maxDevices) {
      results.put(maxDeviceConfiguration.getNumber(),
                  maxDeviceConfiguration.getCount());
    }

    return results;
  }

  public PaymentsServiceConfiguration getPaymentsServiceConfiguration() {
    return paymentsService;
  }

  public ZkConfig getZkConfig() {
    return zkConfig;
  }

  public GenericZkConfig getCallingZkConfig() {
    return callingZkConfig;
  }

  public GenericZkConfig getBackupsZkConfig() {
    return backupsZkConfig;
  }

  public RemoteConfigConfiguration getRemoteConfigConfiguration() {
    return remoteConfig;
  }

  public S3ObjectMonitorFactory getDynamicConfig() {
    return dynamicConfig;
  }

  public BadgesConfiguration getBadges() {
    return badges;
  }

  public SubscriptionConfiguration getSubscription() {
    return subscription;
  }

  public OneTimeDonationConfiguration getOneTimeDonations() {
    return oneTimeDonations;
  }

  public PagedSingleUseKEMPreKeyStoreConfiguration getPagedSingleUseKEMPreKeyStore() {
    return pagedSingleUseKEMPreKeyStore;
  }

  public ReportMessageConfiguration getReportMessageConfiguration() {
    return reportMessage;
  }

  public SpamFilterConfiguration getSpamFilterConfiguration() {
    return spamFilter;
  }

  public RegistrationServiceClientFactory getRegistrationServiceConfiguration() {
    return registrationService;
  }

  public TurnConfiguration getTurnConfiguration() {
    return turn;
  }

  public TusConfiguration getTus() {
    return tus;
  }

  public ClientReleaseConfiguration getClientReleaseConfiguration() {
    return clientRelease;
  }

  public MessageByteLimitCardinalityEstimatorConfiguration getMessageByteLimitCardinalityEstimator() {
    return messageByteLimitCardinalityEstimator;
  }

  public LinkDeviceSecretConfiguration getLinkDeviceSecretConfiguration() {
    return linkDevice;
  }

  public VirtualThreadConfiguration getVirtualThreadConfiguration() {
    return virtualThread;
  }

  public NoiseTunnelConfiguration getNoiseTunnelConfiguration() {
    return noiseTunnel;
  }

  public ExternalRequestFilterConfiguration getExternalRequestFilterConfiguration() {
    return externalRequestFilter;
  }

  public KeyTransparencyServiceConfiguration getKeyTransparencyServiceConfiguration() {
    return keyTransparencyService;
  }

  public boolean logMessageDeliveryLoops() {
    return logMessageDeliveryLoops;
  }

  public IdlePrimaryDeviceReminderConfiguration idlePrimaryDeviceReminderConfiguration() {
    return idlePrimaryDeviceReminder;
  }
}
