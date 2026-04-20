package ai.auralog.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class Logger {
  private final String environment;
  private final Consumer<LogEntry> sink;

  public Logger(String environment, Consumer<LogEntry> sink) {
    this.environment = environment;
    this.sink = sink;
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
    sink.accept(
        new LogEntry(level, message, environment, Instant.now().toString(), metadata, stack));
  }
}
