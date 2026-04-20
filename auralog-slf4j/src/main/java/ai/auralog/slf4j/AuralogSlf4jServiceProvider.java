package ai.auralog.slf4j;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * Registered as the {@code org.slf4j.spi.SLF4JServiceProvider} for SLF4J 2.0+. SLF4J picks this up
 * automatically via the {@code ServiceLoader} SPI when the {@code auralog-slf4j} artifact is on the
 * classpath.
 */
public final class AuralogSlf4jServiceProvider implements SLF4JServiceProvider {
  public static final String REQUESTED_API_VERSION = "2.0.99";

  private ILoggerFactory loggerFactory;
  private IMarkerFactory markerFactory;
  private MDCAdapter mdcAdapter;

  @Override
  public void initialize() {
    loggerFactory = new AuralogSlf4jLoggerFactory();
    markerFactory = new BasicMarkerFactory();
    mdcAdapter = new NOPMDCAdapter();
  }

  @Override
  public ILoggerFactory getLoggerFactory() {
    return loggerFactory;
  }

  @Override
  public IMarkerFactory getMarkerFactory() {
    return markerFactory;
  }

  @Override
  public MDCAdapter getMDCAdapter() {
    return mdcAdapter;
  }

  @Override
  public String getRequestedApiVersion() {
    return REQUESTED_API_VERSION;
  }
}
