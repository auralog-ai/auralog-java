package ai.auralog.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public final class Transport {
  private final String apiKey;
  private final String endpoint;
  private final Duration flushInterval;
  private final HttpClient client;
  private final List<LogEntry> buffer = new ArrayList<>();
  private final ReentrantLock lock = new ReentrantLock();
  private final Thread thread;
  private volatile boolean stopped = false;

  public Transport(String apiKey, String endpoint, Duration flushInterval) {
    this(
        apiKey,
        endpoint,
        flushInterval,
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
  }

  // Package-private: tests inject a stub HttpClient when useful.
  Transport(String apiKey, String endpoint, Duration flushInterval, HttpClient client) {
    this.apiKey = apiKey;
    this.endpoint = stripTrailingSlash(endpoint);
    this.flushInterval = flushInterval;
    this.client = client;
    this.thread = new Thread(this::run, "auralog-flush");
    this.thread.setDaemon(true);
    this.thread.start();
  }

  private static String stripTrailingSlash(String s) {
    return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
  }

  private void run() {
    while (!stopped) {
      try {
        Thread.sleep(flushInterval.toMillis());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return;
      }
      try {
        flush();
      } catch (Exception ignored) {
        // Never let the background thread die on a transient failure.
      }
    }
  }

  public void send(LogEntry entry) {
    if (entry.level().isAtOrAbove(LogLevel.ERROR)) {
      sendSingle(entry);
      return;
    }
    lock.lock();
    try {
      buffer.add(entry);
    } finally {
      lock.unlock();
    }
  }

  public void flush() {
    List<LogEntry> batch;
    lock.lock();
    try {
      if (buffer.isEmpty()) return;
      batch = new ArrayList<>(buffer);
      buffer.clear();
    } finally {
      lock.unlock();
    }
    try {
      List<Map<String, Object>> wireLogs =
          batch.stream().map(Transport::toWire).collect(Collectors.toList());
      Map<String, Object> body = new HashMap<>();
      body.put("projectApiKey", apiKey);
      body.put("logs", wireLogs);
      client.send(
          HttpRequest.newBuilder(URI.create(endpoint + "/v1/logs"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(Json.encode(body)))
              .build(),
          BodyHandlers.discarding());
    } catch (Exception ignored) {
      // Swallow: a single send failure must never crash the host.
    }
  }

  private void sendSingle(LogEntry entry) {
    try {
      Map<String, Object> body = new HashMap<>();
      body.put("projectApiKey", apiKey);
      body.put("log", toWire(entry));
      client.send(
          HttpRequest.newBuilder(URI.create(endpoint + "/v1/logs/single"))
              .header("Content-Type", "application/json")
              .POST(HttpRequest.BodyPublishers.ofString(Json.encode(body)))
              .build(),
          BodyHandlers.discarding());
    } catch (Exception ignored) {
    }
  }

  public void shutdown() {
    stopped = true;
    thread.interrupt();
    try {
      thread.join(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    flush();
  }

  private static Map<String, Object> toWire(LogEntry e) {
    Map<String, Object> m = new HashMap<>();
    m.put("level", e.level().wireName());
    m.put("message", e.message());
    m.put("environment", e.environment());
    m.put("timestamp", e.timestamp());
    if (e.metadata() != null) m.put("metadata", e.metadata());
    if (e.stackTrace() != null) m.put("stackTrace", e.stackTrace());
    return m;
  }
}
