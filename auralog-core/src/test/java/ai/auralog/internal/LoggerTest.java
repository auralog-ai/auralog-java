package ai.auralog.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoggerTest {
  @Test
  void levelsAndMetadata() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add);
    log.info("hi", Map.of("k", "v"));
    log.warn("slow", null);
    log.error("boom", null, null);

    assertThat(out).hasSize(3);
    assertThat(out.get(0).level()).isEqualTo(LogLevel.INFO);
    assertThat(out.get(0).metadata()).isEqualTo(Map.of("k", "v"));
    assertThat(out.get(1).level()).isEqualTo(LogLevel.WARN);
    assertThat(out.get(2).level()).isEqualTo(LogLevel.ERROR);
    assertThat(out.get(1).metadata()).isNull();
  }

  @Test
  void exceptionAttachesStack() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add);
    try {
      throw new RuntimeException("boom");
    } catch (RuntimeException e) {
      log.error("crashed", null, e);
    }
    assertThat(out.get(0).stackTrace()).contains("RuntimeException: boom");
  }

  @Test
  void timestampIsISO8601() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add);
    log.info("hi", null);
    assertThat(out.get(0).timestamp()).contains("T").endsWith("Z");
  }

  @Test
  void environmentPropagated() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("staging", out::add);
    log.info("hi", null);
    assertThat(out.get(0).environment()).isEqualTo("staging");
  }
}
