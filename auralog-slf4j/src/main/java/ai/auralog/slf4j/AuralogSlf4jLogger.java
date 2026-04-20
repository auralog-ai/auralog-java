package ai.auralog.slf4j;

import ai.auralog.Auralog;
import java.util.Collections;
import java.util.Map;
import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.AbstractLogger;
import org.slf4j.helpers.MessageFormatter;

/**
 * SLF4J {@code Logger} implementation that forwards records to the Auralog core facade. Returned by
 * {@link AuralogSlf4jLoggerFactory}; not constructed directly by users.
 */
public final class AuralogSlf4jLogger extends AbstractLogger {
  AuralogSlf4jLogger(String name) {
    this.name = name;
  }

  @Override
  public boolean isTraceEnabled() {
    return true;
  }

  @Override
  public boolean isTraceEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public boolean isDebugEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  @Override
  public boolean isInfoEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isWarnEnabled() {
    return true;
  }

  @Override
  public boolean isWarnEnabled(Marker marker) {
    return true;
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public boolean isErrorEnabled(Marker marker) {
    return true;
  }

  @Override
  protected String getFullyQualifiedCallerName() {
    return AuralogSlf4jLogger.class.getName();
  }

  @Override
  protected void handleNormalizedLoggingCall(
      Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
    String message = MessageFormatter.basicArrayFormat(messagePattern, arguments);
    Map<String, Object> metadata = Collections.singletonMap("logger", name);
    switch (level) {
      case TRACE:
      case DEBUG:
        Auralog.debug(message, metadata);
        break;
      case INFO:
        Auralog.info(message, metadata);
        break;
      case WARN:
        Auralog.warn(message, metadata);
        break;
      case ERROR:
        if (throwable != null) Auralog.error(message, metadata, throwable);
        else Auralog.error(message, metadata);
        break;
      default:
        Auralog.info(message, metadata);
    }
  }
}
