# auralog-java

Java SDK for [Auralog](https://auralog.ai) — agentic logging and application awareness.

Auralog uses Claude as an on-call engineer: it monitors your logs and errors, alerts you when something's wrong, and opens fix PRs automatically.

[![Maven Central](https://img.shields.io/maven-central/v/ai.auralog/auralog-core.svg?label=maven-central&color=blue)](https://central.sonatype.com/artifact/ai.auralog/auralog-core)
[![provenance verified](https://img.shields.io/badge/provenance-verified-2dba4e?logo=sigstore&logoColor=white)](https://central.sonatype.com/artifact/ai.auralog/auralog-core)
[![Java](https://img.shields.io/badge/java-11%20%7C%2017%20%7C%2021%20%7C%2025-blue.svg)](https://central.sonatype.com/artifact/ai.auralog/auralog-core)
[![license](https://img.shields.io/badge/license-MIT-blue.svg)](./LICENSE)

## Install

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("ai.auralog:auralog-core:0.1.0")
    // Optional: stdlib SLF4J bridge — captures logs from Logback/Log4j2/libraries
    implementation("ai.auralog:auralog-slf4j:0.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>ai.auralog</groupId>
    <artifactId>auralog-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

Java 11 or later.

## Quick start

```java
import ai.auralog.Auralog;
import ai.auralog.AuralogConfig;
import java.util.Map;

Auralog.init(AuralogConfig.builder()
    .apiKey(System.getenv("AURALOG_API_KEY"))
    .environment("production")
    .build());

Auralog.info("user signed in", Map.of("userId", "123"));
Auralog.error("payment failed", Map.of("orderId", "abc"));
```

## SLF4J bridge (recommended for existing codebases)

Drop `ai.auralog:auralog-slf4j` on your classpath and any SLF4J call — including from third-party libraries (Logback, Log4j2, Spring, Hibernate, etc.) — flows to Auralog automatically:

```java
import ai.auralog.Auralog;
import ai.auralog.AuralogConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

Auralog.init(AuralogConfig.builder().apiKey("...").environment("production").build());

Logger log = LoggerFactory.getLogger(MyClass.class);
log.info("user signed in {}", userId);
log.error("payment failed", exception);
```

## Configuration

| Option | Type | Default | Description |
|---|---|---|---|
| `apiKey` | `String` | _required_ | Your Auralog project API key |
| `environment` | `String` | `"production"` | e.g. `"production"`, `"staging"`, `"dev"` |
| `endpoint` | `String` | `https://ingest.auralog.ai` | Ingest endpoint override |
| `flushInterval` | `Duration` | `Duration.ofSeconds(5)` | Time between batched flushes (errors flush immediately) |
| `captureErrors` | `boolean` | `true` | Capture uncaught exceptions via `Thread.UncaughtExceptionHandler` |
| `traceId` | `String` | _auto-generated_ | Custom trace ID for distributed tracing |

## Attaching exceptions

```java
try {
    risky();
} catch (Exception e) {
    Auralog.error("task crashed", Map.of("task", "ingest"), e);
}
```

## Graceful shutdown

`Auralog.init()` registers a JVM shutdown hook that flushes pending logs on process exit. For deterministic flush (short-lived CLI apps, serverless):

```java
Auralog.shutdown();
```

## Thread and async safety

- **Multi-threaded apps** (Tomcat, Jetty, Spring Boot) are supported out of the box — the transport is `ReentrantLock`-guarded.
- **Background flushing** runs on a named daemon thread (`auralog-flush`); won't prevent JVM shutdown.
- **Network failures are swallowed** — a single ingest blip never crashes the host app.

## GraalVM native-image

The `auralog-core` artifact ships [reachability metadata](https://docs.oracle.com/en/graalvm/jdk/21/docs/reference-manual/native-image/metadata/) under `META-INF/native-image/ai.auralog/auralog-core/`, so Spring Boot 3 + GraalVM users work with zero extra configuration.

## Verify this package

Every release is published with sigstore provenance attestations via GitHub Actions OIDC. The attestation proves the artifact was built from a specific commit in this repository.

Inspect on [central.sonatype.com/artifact/ai.auralog/auralog-core](https://central.sonatype.com/artifact/ai.auralog/auralog-core).

## Documentation

Full docs at [docs.auralog.ai/java-sdk/installation](https://docs.auralog.ai/java-sdk/installation/).

## Security

Found a vulnerability? See [SECURITY.md](./SECURITY.md) for how to report it.

## License

[MIT](./LICENSE) © James Thomas
