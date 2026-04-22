package ai.auralog;

import java.time.Duration;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable configuration for the Auralog SDK. Build via {@link #builder()}.
 *
 * <pre>{@code
 * AuralogConfig config = AuralogConfig.builder()
 *     .apiKey(System.getenv("AURALOG_API_KEY"))
 *     .environment("production")
 *     .build();
 * Auralog.init(config);
 * }</pre>
 */
public final class AuralogConfig {
  private static final String DEFAULT_ENDPOINT = "https://ingest.auralog.ai";
  private static final Duration DEFAULT_FLUSH_INTERVAL = Duration.ofSeconds(5);
  private static final String DEFAULT_ENVIRONMENT = "production";

  private final String apiKey;
  private final String environment;
  private final String endpoint;
  private final Duration flushInterval;
  private final boolean captureErrors;
  private final @Nullable String traceId;

  private AuralogConfig(Builder b) {
    this.apiKey = Objects.requireNonNull(b.apiKey, "apiKey");
    this.environment = b.environment;
    this.endpoint = b.endpoint;
    this.flushInterval = b.flushInterval;
    this.captureErrors = b.captureErrors;
    this.traceId = b.traceId;
  }

  public String apiKey() {
    return apiKey;
  }

  public String environment() {
    return environment;
  }

  public String endpoint() {
    return endpoint;
  }

  public Duration flushInterval() {
    return flushInterval;
  }

  public boolean captureErrors() {
    return captureErrors;
  }

  public @Nullable String traceId() {
    return traceId;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Fluent builder for {@link AuralogConfig}. Every setter returns {@code this}. */
  public static final class Builder {
    private String apiKey;
    private String environment = DEFAULT_ENVIRONMENT;
    private String endpoint = DEFAULT_ENDPOINT;
    private Duration flushInterval = DEFAULT_FLUSH_INTERVAL;
    private boolean captureErrors = true;
    private @Nullable String traceId;

    public Builder apiKey(String v) {
      this.apiKey = v;
      return this;
    }

    public Builder environment(String v) {
      this.environment = v;
      return this;
    }

    public Builder endpoint(String v) {
      this.endpoint = v;
      return this;
    }

    public Builder flushInterval(Duration v) {
      this.flushInterval = v;
      return this;
    }

    public Builder captureErrors(boolean v) {
      this.captureErrors = v;
      return this;
    }

    public Builder traceId(String v) {
      this.traceId = v;
      return this;
    }

    public AuralogConfig build() {
      return new AuralogConfig(this);
    }
  }
}
