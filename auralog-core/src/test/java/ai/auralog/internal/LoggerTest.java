package ai.auralog.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoggerTest {
  @Test
  void levelsAndMetadata() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null);
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
    Logger log = new Logger("prod", out::add, null);
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
    Logger log = new Logger("prod", out::add, null);
    log.info("hi", null);
    assertThat(out.get(0).timestamp()).contains("T").endsWith("Z");
  }

  @Test
  void environmentPropagated() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("staging", out::add, null);
    log.info("hi", null);
    assertThat(out.get(0).environment()).isEqualTo("staging");
  }

  @Test
  void autoGeneratesTraceId() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null);
    log.info("hi", null);
    assertThat(out.get(0).traceId()).isNotNull().isNotEmpty();
  }

  @Test
  void usesProvidedTraceId() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, "my-trace-123");
    log.info("hi", null);
    assertThat(out.get(0).traceId()).isEqualTo("my-trace-123");
  }

  @Test
  void perLogTraceIdOverrideViaMetadata() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null);
    Map<String, Object> meta = new HashMap<>();
    meta.put("traceId", "override-456");
    meta.put("extra", "data");
    log.info("hi", meta);
    assertThat(out.get(0).traceId()).isEqualTo("override-456");
    assertThat(out.get(0).metadata()).containsEntry("extra", "data");
    assertThat(out.get(0).metadata()).doesNotContainKey("traceId");
  }

  @Test
  void getAndSetTraceId() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null);
    String original = log.getTraceId();
    assertThat(original).isNotEmpty();
    log.setTraceId("new-trace-789");
    assertThat(log.getTraceId()).isEqualTo("new-trace-789");
    log.info("hi", null);
    assertThat(out.get(0).traceId()).isEqualTo("new-trace-789");
  }
}
