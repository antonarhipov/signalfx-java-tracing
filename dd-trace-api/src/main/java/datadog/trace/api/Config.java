// Modified by SignalFx
package datadog.trace.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.UUID;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * Config gives priority to system properties and falls back to environment variables. It also
 * includes default values to ensure a valid config.
 *
 * <p>
 *
 * <p>System properties are {@link Config#PREFIX}'ed. Environment variables are the same as the
 * system property, but uppercased with '.' -> '_'.
 */
@Slf4j
@ToString(includeFieldNames = true)
public class Config {
  /** Config keys below */
  private static final String PREFIX = "dd.";

  private static final String SIGNALFX_PREFIX = "signalfx.";

  private static final Pattern ENV_REPLACEMENT = Pattern.compile("[^a-zA-Z0-9_]");

  private static final Config INSTANCE = new Config();

  public static final String SERVICE_NAME = "service.name";
  public static final String SERVICE = "service";
  public static final String WRITER_TYPE = "writer.type";
  public static final String API_TYPE = "api.type";
  public static final String USE_B3_PROPAGATION = "b3.propagation";
  public static final String AGENT_HOST = "agent.host";
  public static final String TRACE_AGENT_PORT = "trace.agent.port";
  public static final String AGENT_PORT_LEGACY = "agent.port";
  public static final String AGENT_PATH = "agent.path";
  public static final String AGENT_USE_HTTPS = "agent.https";
  public static final String ENDPOINT_URL = "endpoint.url";
  public static final String PRIORITY_SAMPLING = "priority.sampling";
  public static final String TRACE_RESOLVER_ENABLED = "trace.resolver.enabled";
  public static final String SERVICE_MAPPING = "service.mapping";
  public static final String GLOBAL_TAGS = "trace.global.tags";
  public static final String SPAN_TAGS = "trace.span.tags";
  public static final String JMX_TAGS = "trace.jmx.tags";
  public static final String TRACE_ANALYTICS_ENABLED = "trace.analytics.enabled";
  public static final String TRACE_ANNOTATIONS = "trace.annotations";
  public static final String TRACE_METHODS = "trace.methods";
  public static final String HEADER_TAGS = "trace.header.tags";
  public static final String HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN = "trace.http.client.split-by-domain";
  public static final String PARTIAL_FLUSH_MIN_SPANS = "trace.partial.flush.min.spans";
  public static final String RUNTIME_CONTEXT_FIELD_INJECTION =
      "trace.runtime.context.field.injection";
  public static final String JMX_FETCH_ENABLED = "jmxfetch.enabled";
  public static final String JMX_FETCH_METRICS_CONFIGS = "jmxfetch.metrics-configs";
  public static final String JMX_FETCH_CHECK_PERIOD = "jmxfetch.check-period";
  public static final String JMX_FETCH_REFRESH_BEANS_PERIOD = "jmxfetch.refresh-beans-period";
  public static final String JMX_FETCH_STATSD_HOST = "jmxfetch.statsd.host";
  public static final String JMX_FETCH_STATSD_PORT = "jmxfetch.statsd.port";
  public static final String APP_CUSTOM_LOG_MANAGER = "app.customlogmanager";

  public static final String RUNTIME_ID_TAG = "runtime-id";
  public static final String LANGUAGE_TAG_KEY = "language";
  public static final String LANGUAGE_TAG_VALUE = "jvm";
  public static final String DEFAULT_SERVICE_NAME = "unnamed-java-app";

  public static final String DD_AGENT_WRITER_TYPE = "DDAgentWriter";
  public static final String DD_AGENT_API_TYPE = "DD";
  public static final String ZIPKIN_V2_API_TYPE = "ZipkinV2";
  public static final String LOGGING_WRITER_TYPE = "LoggingWriter";
  public static final String DEFAULT_AGENT_WRITER_TYPE = DD_AGENT_WRITER_TYPE;
  public static final String DEFAULT_API_TYPE = ZIPKIN_V2_API_TYPE;

  public static final String DEFAULT_AGENT_ENDPOINT = "http://localhost:9080/v1/trace";

  public static final String LOGS_INJECTION_ENABLED = "logs.injection";
  public static final boolean DEFAULT_LOGS_INJECTION_ENABLED = false;

  private static final boolean DEFAULT_RUNTIME_CONTEXT_FIELD_INJECTION = true;

  private static final boolean DEFAULT_PRIORITY_SAMPLING_ENABLED = false;
  private static final boolean DEFAULT_TRACE_RESOLVER_ENABLED = true;
  private static final boolean DEFAULT_HTTP_CLIENT_SPLIT_BY_DOMAIN = false;
  private static final int DEFAULT_PARTIAL_FLUSH_MIN_SPANS = 0;
  private static final boolean DEFAULT_JMX_FETCH_ENABLED = false;

  public static final int DEFAULT_JMX_FETCH_STATSD_PORT = 8125;

  private static final boolean DEFAULT_APP_CUSTOM_LOG_MANAGER = false;

  /**
   * this is a random UUID that gets generated on JVM start up and is attached to every root span
   * and every JMX metric that is sent out.
   */
  @Getter private final String runtimeId;

  @Getter private final String serviceName;
  @Getter private final String writerType;
  @Getter private final String apiType;
  @Getter private final boolean useB3Propagation;
  private final String agentHost;
  private final Integer agentPort;
  private final String agentPath;
  private final Boolean agentUseHTTPS;
  @Getter private final URL endpointUrl;
  @Getter private final boolean prioritySamplingEnabled;
  @Getter private final boolean traceResolverEnabled;
  @Getter private final Map<String, String> serviceMapping;
  private final Map<String, String> globalTags;
  private final Map<String, String> spanTags;
  private final Map<String, String> jmxTags;
  @Getter private final Map<String, String> headerTags;
  @Getter private final boolean httpClientSplitByDomain;
  @Getter private final Integer partialFlushMinSpans;
  @Getter private final boolean runtimeContextFieldInjection;
  @Getter private final boolean jmxFetchEnabled;
  @Getter private final List<String> jmxFetchMetricsConfigs;
  @Getter private final Integer jmxFetchCheckPeriod;
  @Getter private final Integer jmxFetchRefreshBeansPeriod;
  @Getter private final String jmxFetchStatsdHost;
  @Getter private final Integer jmxFetchStatsdPort;
  @Getter private final boolean logsInjectionEnabled;
  @Getter private final boolean appCustomLogManager;

  // Read order: System Properties -> Env Variables, [-> default value]
  // Visible for testing
  Config() {
    runtimeId = UUID.randomUUID().toString();

    serviceName = getSettingFromEnvironment(SERVICE_NAME, DEFAULT_SERVICE_NAME);
    writerType = getSettingFromEnvironment(WRITER_TYPE, DEFAULT_AGENT_WRITER_TYPE);
    apiType = getSettingFromEnvironment(API_TYPE, DEFAULT_API_TYPE);
    useB3Propagation = getBooleanSettingFromEnvironment(USE_B3_PROPAGATION, true);
    agentHost = getSettingFromEnvironment(AGENT_HOST, null);
    agentPort =
        getIntegerSettingFromEnvironment(
            TRACE_AGENT_PORT, getIntegerSettingFromEnvironment(AGENT_PORT_LEGACY, null));
    agentPath = getSettingFromEnvironment(AGENT_PATH, null);
    agentUseHTTPS = getBooleanSettingFromEnvironment(AGENT_USE_HTTPS, null);
    endpointUrl = getURLSettingFromEnvironment(ENDPOINT_URL, DEFAULT_AGENT_ENDPOINT);
    prioritySamplingEnabled =
        getBooleanSettingFromEnvironment(PRIORITY_SAMPLING, DEFAULT_PRIORITY_SAMPLING_ENABLED);
    traceResolverEnabled =
        getBooleanSettingFromEnvironment(TRACE_RESOLVER_ENABLED, DEFAULT_TRACE_RESOLVER_ENABLED);
    serviceMapping = getMapSettingFromEnvironment(SERVICE_MAPPING, null);

    globalTags = getMapSettingFromEnvironment(GLOBAL_TAGS, null);
    spanTags = getMapSettingFromEnvironment(SPAN_TAGS, null);
    jmxTags = getMapSettingFromEnvironment(JMX_TAGS, null);
    headerTags = getMapSettingFromEnvironment(HEADER_TAGS, null);

    httpClientSplitByDomain =
        getBooleanSettingFromEnvironment(
            HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, DEFAULT_HTTP_CLIENT_SPLIT_BY_DOMAIN);

    partialFlushMinSpans =
        getIntegerSettingFromEnvironment(PARTIAL_FLUSH_MIN_SPANS, DEFAULT_PARTIAL_FLUSH_MIN_SPANS);

    runtimeContextFieldInjection =
        getBooleanSettingFromEnvironment(
            RUNTIME_CONTEXT_FIELD_INJECTION, DEFAULT_RUNTIME_CONTEXT_FIELD_INJECTION);

    jmxFetchEnabled =
        getBooleanSettingFromEnvironment(JMX_FETCH_ENABLED, DEFAULT_JMX_FETCH_ENABLED);
    jmxFetchMetricsConfigs = getListSettingFromEnvironment(JMX_FETCH_METRICS_CONFIGS, null);
    jmxFetchCheckPeriod = getIntegerSettingFromEnvironment(JMX_FETCH_CHECK_PERIOD, null);
    jmxFetchRefreshBeansPeriod =
        getIntegerSettingFromEnvironment(JMX_FETCH_REFRESH_BEANS_PERIOD, null);
    jmxFetchStatsdHost = getSettingFromEnvironment(JMX_FETCH_STATSD_HOST, null);
    jmxFetchStatsdPort =
        getIntegerSettingFromEnvironment(JMX_FETCH_STATSD_PORT, DEFAULT_JMX_FETCH_STATSD_PORT);

    logsInjectionEnabled =
        getBooleanSettingFromEnvironment(LOGS_INJECTION_ENABLED, DEFAULT_LOGS_INJECTION_ENABLED);

    appCustomLogManager =
        getBooleanSettingFromEnvironment(APP_CUSTOM_LOG_MANAGER, DEFAULT_APP_CUSTOM_LOG_MANAGER);
  }

  // Read order: Properties -> Parent
  private Config(final Properties properties, final Config parent) {
    runtimeId = parent.runtimeId;

    serviceName = properties.getProperty(SERVICE_NAME, parent.serviceName);
    writerType = properties.getProperty(WRITER_TYPE, parent.writerType);
    apiType = properties.getProperty(API_TYPE, parent.apiType);
    useB3Propagation =
        getPropertyBooleanValue(properties, USE_B3_PROPAGATION, parent.useB3Propagation);
    agentHost = properties.getProperty(AGENT_HOST, parent.agentHost);
    agentPort =
        getPropertyIntegerValue(
            properties,
            TRACE_AGENT_PORT,
            getPropertyIntegerValue(properties, AGENT_PORT_LEGACY, parent.agentPort));
    agentPath = properties.getProperty(AGENT_PATH, parent.agentPath);
    agentUseHTTPS = getPropertyBooleanValue(properties, AGENT_USE_HTTPS, parent.agentUseHTTPS);
    endpointUrl = getPropertyURLValue(properties, ENDPOINT_URL, parent.endpointUrl);
    prioritySamplingEnabled =
        getPropertyBooleanValue(properties, PRIORITY_SAMPLING, parent.prioritySamplingEnabled);
    traceResolverEnabled =
        getPropertyBooleanValue(properties, TRACE_RESOLVER_ENABLED, parent.traceResolverEnabled);
    serviceMapping = getPropertyMapValue(properties, SERVICE_MAPPING, parent.serviceMapping);

    globalTags = getPropertyMapValue(properties, GLOBAL_TAGS, parent.globalTags);
    spanTags = getPropertyMapValue(properties, SPAN_TAGS, parent.spanTags);
    jmxTags = getPropertyMapValue(properties, JMX_TAGS, parent.jmxTags);
    headerTags = getPropertyMapValue(properties, HEADER_TAGS, parent.headerTags);

    httpClientSplitByDomain =
        getPropertyBooleanValue(
            properties, HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, parent.httpClientSplitByDomain);

    partialFlushMinSpans =
        getPropertyIntegerValue(properties, PARTIAL_FLUSH_MIN_SPANS, parent.partialFlushMinSpans);

    runtimeContextFieldInjection =
        getPropertyBooleanValue(
            properties, RUNTIME_CONTEXT_FIELD_INJECTION, parent.runtimeContextFieldInjection);

    jmxFetchEnabled =
        getPropertyBooleanValue(properties, JMX_FETCH_ENABLED, parent.jmxFetchEnabled);
    jmxFetchMetricsConfigs =
        getPropertyListValue(properties, JMX_FETCH_METRICS_CONFIGS, parent.jmxFetchMetricsConfigs);
    jmxFetchCheckPeriod =
        getPropertyIntegerValue(properties, JMX_FETCH_CHECK_PERIOD, parent.jmxFetchCheckPeriod);
    jmxFetchRefreshBeansPeriod =
        getPropertyIntegerValue(
            properties, JMX_FETCH_REFRESH_BEANS_PERIOD, parent.jmxFetchRefreshBeansPeriod);
    jmxFetchStatsdHost = properties.getProperty(JMX_FETCH_STATSD_HOST, parent.jmxFetchStatsdHost);
    jmxFetchStatsdPort =
        getPropertyIntegerValue(properties, JMX_FETCH_STATSD_PORT, parent.jmxFetchStatsdPort);

    logsInjectionEnabled =
        getBooleanSettingFromEnvironment(LOGS_INJECTION_ENABLED, DEFAULT_LOGS_INJECTION_ENABLED);

    appCustomLogManager =
        getBooleanSettingFromEnvironment(APP_CUSTOM_LOG_MANAGER, DEFAULT_APP_CUSTOM_LOG_MANAGER);
  }

  public String getAgentHost() {
    if (agentHost != null) {
      return agentHost;
    }
    return endpointUrl == null ? null : endpointUrl.getHost();
  }

  public Integer getAgentPort() {
    if (agentPort != null) {
      return agentPort;
    }
    return endpointUrl == null ? null : endpointUrl.getPort();
  }

  public Boolean getAgentUseHTTPS() {
    if (agentUseHTTPS != null) {
      return agentUseHTTPS;
    }
    return endpointUrl == null ? null : "https".equals(endpointUrl.getProtocol());
  }

  public String getAgentPath() {
    if (agentPath != null) {
      return agentPath;
    }
    return endpointUrl == null ? null : endpointUrl.getPath();
  }

  public Map<String, String> getMergedSpanTags() {
    // DO not include runtimeId into span tags: we only want that added to the root span
    final Map<String, String> result = newHashMap(globalTags.size() + spanTags.size());
    result.putAll(globalTags);
    result.putAll(spanTags);
    return Collections.unmodifiableMap(result);
  }

  public Map<String, String> getMergedJmxTags() {
    final Map<String, String> runtimeTags = getRuntimeTags();
    final Map<String, String> result =
        newHashMap(
            globalTags.size() + jmxTags.size() + runtimeTags.size() + 1 /* for serviceName */);
    result.putAll(globalTags);
    result.putAll(jmxTags);
    result.putAll(runtimeTags);
    // service name set here instead of getRuntimeTags because apm already manages the service tag
    // and may chose to override it.
    // Additionally, infra/JMX metrics require `service` rather than APM's `service.name` tag
    result.put(SERVICE, serviceName);
    return Collections.unmodifiableMap(result);
  }

  /**
   * Return a map of tags required by the datadog backend to link runtime metrics (i.e. jmx) and
   * traces.
   *
   * <p>These tags must be applied to every runtime metrics and placed on the root span of every
   * trace.
   *
   * @return A map of tag-name -> tag-value
   */
  public Map<String, String> getRuntimeTags() {
    final Map<String, String> result = newHashMap(2);
    result.put(RUNTIME_ID_TAG, runtimeId);
    result.put(LANGUAGE_TAG_KEY, LANGUAGE_TAG_VALUE);
    return Collections.unmodifiableMap(result);
  }

  public static String getSettingFromEnvironment(final String name, final String defaultValue) {
    String value = getSettingFromEnvironment(PREFIX, name, null);
    if (value == null) {
      // Let the SignalFx prefix act as a fallback so we support both for testing/migration
      // purposes.
      value = getSettingFromEnvironment(SIGNALFX_PREFIX, name, null);
    }
    return value == null ? defaultValue : value;
  }

  public static boolean integrationEnabled(
      final SortedSet<String> integrationNames, final boolean defaultEnabled) {
    // If default is enabled, we want to enable individually,
    // if default is disabled, we want to disable individually.
    boolean anyEnabled = defaultEnabled;
    for (final String name : integrationNames) {
      final boolean configEnabled =
          getBooleanSettingFromEnvironment("integration." + name + ".enabled", defaultEnabled);
      if (defaultEnabled) {
        anyEnabled &= configEnabled;
      } else {
        anyEnabled |= configEnabled;
      }
    }
    return anyEnabled;
  }

  public static boolean traceAnalyticsIntegrationEnabled(
      final SortedSet<String> integrationNames, final boolean defaultEnabled) {
    // If default is enabled, we want to enable individually,
    // if default is disabled, we want to disable individually.
    boolean anyEnabled = defaultEnabled;
    for (final String name : integrationNames) {
      final boolean configEnabled =
          getBooleanSettingFromEnvironment(
              "integration." + name + ".analytics.enabled", defaultEnabled);
      if (defaultEnabled) {
        anyEnabled &= configEnabled;
      } else {
        anyEnabled |= configEnabled;
      }
    }
    return anyEnabled;
  }

  /**
   * Helper method that takes the name, adds a "dd." prefix then checks for System Properties of
   * that name. If none found, the name is converted to an Environment Variable and used to check
   * the env. If setting not configured in either location, defaultValue is returned.
   *
   * @param name
   * @param defaultValue
   * @return
   */
  public static String getSettingFromEnvironment(
      final String prefix, final String name, final String defaultValue) {
    final String completeName = prefix + name;
    final String value =
        System.getProperties()
            .getProperty(completeName, System.getenv(propertyToEnvironmentName(completeName)));
    return value == null ? defaultValue : value;
  }

  private static Map<String, String> getMapSettingFromEnvironment(
      final String name, final String defaultValue) {
    return parseMap(getSettingFromEnvironment(name, defaultValue), name);
  }

  private static List<String> getListSettingFromEnvironment(
      final String name, final String defaultValue) {
    return parseList(getSettingFromEnvironment(name, defaultValue));
  }

  private static URL getURLSettingFromEnvironment(final String name, final String defaultValue) {
    return parseURL(getSettingFromEnvironment(name, defaultValue), name);
  }

  /**
   * Calls {@link #getSettingFromEnvironment(String, String)} and converts the result to a Boolean.
   *
   * @param name
   * @param defaultValue
   * @return
   */
  public static Boolean getBooleanSettingFromEnvironment(
      final String name, final Boolean defaultValue) {
    final String value = getSettingFromEnvironment(name, null);
    return value == null || value.trim().isEmpty() ? defaultValue : Boolean.valueOf(value);
  }

  /**
   * Calls {@link #getSettingFromEnvironment(String, String)} and converts the result to a Float.
   *
   * @param name
   * @param defaultValue
   * @return
   */
  public static Float getFloatSettingFromEnvironment(final String name, final Float defaultValue) {
    final String value = getSettingFromEnvironment(name, null);
    try {
      return value == null ? defaultValue : Float.valueOf(value);
    } catch (final NumberFormatException e) {
      log.warn("Invalid configuration for " + name, e);
      return defaultValue;
    }
  }

  private static Integer getIntegerSettingFromEnvironment(
      final String name, final Integer defaultValue) {
    final String value = getSettingFromEnvironment(name, null);
    try {
      return value == null ? defaultValue : Integer.valueOf(value);
    } catch (final NumberFormatException e) {
      log.warn("Invalid configuration for " + name, e);
      return defaultValue;
    }
  }

  private static String propertyToEnvironmentName(final String name) {
    return ENV_REPLACEMENT.matcher(name.toUpperCase()).replaceAll("_");
  }

  private static Map<String, String> getPropertyMapValue(
      final Properties properties, final String name, final Map<String, String> defaultValue) {
    final String value = properties.getProperty(name);
    return value == null || value.trim().isEmpty() ? defaultValue : parseMap(value, name);
  }

  private static List<String> getPropertyListValue(
      final Properties properties, final String name, final List<String> defaultValue) {
    final String value = properties.getProperty(name);
    return value == null || value.trim().isEmpty() ? defaultValue : parseList(value);
  }

  private static URL getPropertyURLValue(
      final Properties properties, final String name, final URL defaultValue) {
    final String value = properties.getProperty(name);
    return value == null ? defaultValue : parseURL(value, name);
  }

  private static Boolean getPropertyBooleanValue(
      final Properties properties, final String name, final Boolean defaultValue) {
    final String value = properties.getProperty(name);
    return value == null || value.trim().isEmpty() ? defaultValue : Boolean.valueOf(value);
  }

  private static Integer getPropertyIntegerValue(
      final Properties properties, final String name, final Integer defaultValue) {
    final String value = properties.getProperty(name);
    return value == null || value.trim().isEmpty() ? defaultValue : Integer.valueOf(value);
  }

  private static Map<String, String> parseMap(final String str, final String settingName) {
    if (str == null || str.trim().isEmpty()) {
      return Collections.emptyMap();
    }
    if (!str.matches("(([^,:]+:[^,:]*,)*([^,:]+:[^,:]*),?)?")) {
      log.warn(
          "Invalid config for {}: '{}'. Must match 'key1:value1,key2:value2'.", settingName, str);
      return Collections.emptyMap();
    }

    final String[] tokens = str.split(",", -1);
    final Map<String, String> map = newHashMap(tokens.length);

    for (final String token : tokens) {
      final String[] keyValue = token.split(":", -1);
      if (keyValue.length == 2) {
        final String key = keyValue[0].trim();
        final String value = keyValue[1].trim();
        if (value.length() <= 0) {
          log.warn("Ignoring empty value for key '{}' in config for {}", key, settingName);
          continue;
        }
        map.put(key, value);
      }
    }
    return Collections.unmodifiableMap(map);
  }

  private static Map<String, String> newHashMap(final int size) {
    return new HashMap<>(size + 1, 1f);
  }

  private static List<String> parseList(final String str) {
    if (str == null || str.trim().isEmpty()) {
      return Collections.emptyList();
    }

    final String[] tokens = str.split(",", -1);
    return Collections.unmodifiableList(Arrays.asList(tokens));
  }

  private static URL parseURL(final String str, final String settingName) {
    try {
      return new URL(str);
    } catch (MalformedURLException e) {
      log.warn("Malformed URL {} in setting {}: {}", str, settingName, e.getMessage());
      return null;
    }
  }

  public static Config get() {
    return INSTANCE;
  }

  public static Config get(final Properties properties) {
    if (properties == null || properties.isEmpty()) {
      return INSTANCE;
    } else {
      return new Config(properties, INSTANCE);
    }
  }
}
