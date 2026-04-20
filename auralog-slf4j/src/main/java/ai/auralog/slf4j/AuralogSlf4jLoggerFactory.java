package ai.auralog.slf4j;

import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

final class AuralogSlf4jLoggerFactory implements ILoggerFactory {
  private final ConcurrentHashMap<String, Logger> loggers = new ConcurrentHashMap<>();

  @Override
  public Logger getLogger(String name) {
    return loggers.computeIfAbsent(name, AuralogSlf4jLogger::new);
  }
}
