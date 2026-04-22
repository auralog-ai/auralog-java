package ai.auralog.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class Logger {
  private final String environment;
  private final Consumer<LogEntry> sink;
  private volatile String traceId;

  public Logger(String environment, Consumer<LogEntry> sink, @Nullable String traceId) {
    this.environment = environment;
    this.sink = sink;
    this.traceId = traceId != null ? traceId : UUID.randomUUID().toString();
  }

  public String getTraceId() {
    return traceId;
  }

  public void setTraceId(String id) {
    this.traceId = id;
  }

  public void debug(String message, @Nullable Map<String, Object> metadata) {
    emit(LogLevel.DEBUG, message, metadata, null);
  }

  public void info(String message, @Nullable Map<String, Object> metadata) {
    emit(LogLevel.INFO, message, metadata, null);
  }

  public void warn(String message, @Nullable Map<String, Object> metadata) {
    emit(LogLevel.WARN, message, metadata, null);
  }

  public void error(String message, @Nullable Map<String, Object> metadata, @Nullable Throwable t) {
    emit(LogLevel.ERROR, message, metadata, t);
  }

  public void fatal(String message, @Nullable Map<String, Object> metadata, @Nullable Throwable t) {
    emit(LogLevel.FATAL, message, metadata, t);
  }

  private void emit(
      LogLevel level,
      String message,
      @Nullable Map<String, Object> metadata,
      @Nullable Throwable t) {
    String stack = null;
    if (t != null) {
      StringWriter sw = new StringWriter();
      t.printStackTrace(new PrintWriter(sw));
      stack = sw.toString();
    }
    String entryTraceId = this.traceId;
    Map<String, Object> cleanMeta = metadata;
    if (metadata != null && metadata.containsKey("traceId")) {
      entryTraceId = String.valueOf(metadata.get("traceId"));
      cleanMeta = new LinkedHashMap<>(metadata);
      cleanMeta.remove("traceId");
      if (cleanMeta.isEmpty()) cleanMeta = null;
    }
    sink.accept(
        new LogEntry(
            level, message, environment, Instant.now().toString(), cleanMeta, stack, entryTraceId));
  }
}
