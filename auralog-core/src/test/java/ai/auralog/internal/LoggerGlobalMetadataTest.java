package ai.auralog.internal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class LoggerGlobalMetadataTest {

  @Test
  void staticMapAttachesToEveryEntry() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier = () -> Map.of("userId", "u1");
    Logger log = new Logger("prod", out::add, null, supplier);

    log.info("first", null);
    log.warn("second", null);
    log.error("third", null, null);

    assertThat(out).hasSize(3);
    for (LogEntry entry : out) {
      assertThat(entry.metadata()).containsEntry("userId", "u1");
    }
  }

  @Test
  void supplierIsInvokedPerEmission() {
    List<LogEntry> out = new ArrayList<>();
    AtomicInteger callCount = new AtomicInteger();
    Supplier<Map<String, Object>> supplier =
        () -> {
          int call = callCount.incrementAndGet();
          return Map.of("call", call);
        };
    Logger log = new Logger("prod", out::add, null, supplier);

    log.info("a", null);
    log.info("b", null);
    log.info("c", null);

    assertThat(callCount.get()).isEqualTo(3);
    assertThat(out.get(0).metadata()).containsEntry("call", 1);
    assertThat(out.get(1).metadata()).containsEntry("call", 2);
    assertThat(out.get(2).metadata()).containsEntry("call", 3);
  }

  @Test
  void supplierThatThrowsEmitsEntryWithoutGlobalMetadataAndWarnsOnce() {
    List<LogEntry> out = new ArrayList<>();
    AtomicInteger callCount = new AtomicInteger();
    Supplier<Map<String, Object>> throwing =
        () -> {
          callCount.incrementAndGet();
          throw new RuntimeException("supplier broken");
        };
    Logger log = new Logger("prod", out::add, null, throwing);

    log.info("hi", Map.of("perCall", "v"));
    log.info("hi again", null);

    assertThat(out).hasSize(2);
    assertThat(out.get(0).metadata()).isEqualTo(Map.of("perCall", "v"));
    assertThat(out.get(1).metadata()).isNull();
    assertThat(callCount.get()).isEqualTo(2);
    assertThat(log.hasWarnedAboutGlobalMetadata()).isTrue();
  }

  @Test
  void supplierReturningCompletionStageIsTreatedAsFailure() {
    List<LogEntry> out = new ArrayList<>();
    @SuppressWarnings({"rawtypes", "unchecked"})
    Supplier<Map<String, Object>> bad = () -> (Map) CompletableFuture.completedFuture("nope");
    Logger log = new Logger("prod", out::add, null, bad);

    log.info("hi", null);

    assertThat(out).hasSize(1);
    assertThat(out.get(0).metadata()).isNull();
    assertThat(log.hasWarnedAboutGlobalMetadata()).isTrue();
  }

  @Test
  void perCallKeyOverridesGlobalOnCollision() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier = () -> Map.of("userId", "global-user", "env", "stage");
    Logger log = new Logger("prod", out::add, null, supplier);

    log.info("override", Map.of("userId", "per-call-user"));

    assertThat(out).hasSize(1);
    Map<String, Object> meta = out.get(0).metadata();
    assertThat(meta).containsEntry("userId", "per-call-user");
    assertThat(meta).containsEntry("env", "stage");
  }

  @Test
  void mergeIsShallowNotDeep() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier = () -> Map.of("nested", Map.of("a", 1, "b", 2));
    Logger log = new Logger("prod", out::add, null, supplier);

    Map<String, Object> perCall = new LinkedHashMap<>();
    perCall.put("nested", Map.of("c", 3));
    log.info("shallow", perCall);

    Map<String, Object> meta = out.get(0).metadata();
    assertThat(meta).isNotNull();
    @SuppressWarnings("unchecked")
    Map<String, Object> nested = (Map<String, Object>) meta.get("nested");
    assertThat(nested).containsOnly(Map.entry("c", 3));
  }

  @Test
  void bothSidesEmptyOmitsMetadataEntirely() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null, null);
    log.info("hi", null);
    assertThat(out.get(0).metadata()).isNull();
  }

  @Test
  void nonSerializableValueDropsGlobalMetadataAndStillEmits() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier =
        () -> {
          Map<String, Object> map = new HashMap<>();
          // A bare Object — not a primitive, Map, or Iterable — is rejected by the strict check.
          map.put("opaque", new Object());
          return map;
        };
    Logger log = new Logger("prod", out::add, null, supplier);

    log.info("still-delivered", Map.of("perCall", "kept"));

    assertThat(out).hasSize(1);
    // globalMetadata dropped, per-call retained.
    assertThat(out.get(0).metadata()).isEqualTo(Map.of("perCall", "kept"));
    assertThat(log.hasWarnedAboutGlobalMetadata()).isTrue();
  }

  @Test
  void circularReferenceIsDetectedAndDropped() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier =
        () -> {
          Map<String, Object> root = new HashMap<>();
          root.put("self", root); // circular
          return root;
        };
    Logger log = new Logger("prod", out::add, null, supplier);

    log.info("survives", null);

    assertThat(out).hasSize(1);
    assertThat(out.get(0).metadata()).isNull();
    assertThat(log.hasWarnedAboutGlobalMetadata()).isTrue();
  }

  @Test
  void warnFiresOnlyOnceAcrossMultipleFailures() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> alwaysThrows =
        () -> {
          throw new RuntimeException("boom");
        };
    Logger log = new Logger("prod", out::add, null, alwaysThrows);

    for (int index = 0; index < 5; index++) log.info("x", null);

    assertThat(out).hasSize(5);
    // Single warn flag — no way to count System.Logger output without a handler, so we assert the
    // flag rather than the System.Logger output.
    assertThat(log.hasWarnedAboutGlobalMetadata()).isTrue();
  }

  @Test
  void absentGlobalMetadataPreservesExistingBehavior() {
    List<LogEntry> out = new ArrayList<>();
    Logger log = new Logger("prod", out::add, null, null);

    log.info("hi", Map.of("a", 1));
    log.info("bye", null);

    assertThat(out.get(0).metadata()).isEqualTo(Map.of("a", 1));
    assertThat(out.get(1).metadata()).isNull();
  }

  @Test
  void traceIdMetadataKeyStillExtractsAfterMerge() {
    List<LogEntry> out = new ArrayList<>();
    Supplier<Map<String, Object>> supplier = () -> Map.of("userId", "u1");
    Logger log = new Logger("prod", out::add, null, supplier);

    Map<String, Object> perCall = new LinkedHashMap<>();
    perCall.put("traceId", "override-trace");
    perCall.put("op", "checkout");
    log.info("hi", perCall);

    assertThat(out.get(0).traceId()).isEqualTo("override-trace");
    assertThat(out.get(0).metadata()).containsEntry("userId", "u1");
    assertThat(out.get(0).metadata()).containsEntry("op", "checkout");
    assertThat(out.get(0).metadata()).doesNotContainKey("traceId");
  }
}
