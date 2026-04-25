# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.2.0] - 2026-04-25

### Added
- `AuralogConfig.Builder.globalMetadata(Supplier<Map<String, Object>>)` — attach session-scoped fields (e.g. `userId`, organisation id, feature-flag snapshot) to every emitted log entry. The supplier is invoked at every emission, never pre-resolved at init, so it late-binds to mutable host state.
- `AuralogConfig.Builder.globalMetadata(Map<String, Object>)` — convenience overload that wraps a static map as `() -> map`. The supplier form remains the load-bearing API.
- Capture-path entries — both the `auralog-slf4j` SLF4J bridge and uncaught-exception captures (`Thread.UncaughtExceptionHandler`) now flow through the same merge choke-point as direct API calls and therefore carry `globalMetadata`. Previously these entries shipped without any session attribution.

### Behaviour
- Per-call metadata wins on key collision with `globalMetadata`. Merge is shallow.
- The supplier is treated as failed when it throws, returns a `CompletionStage` / `CompletableFuture` (the SDK does not await async suppliers), or returns a value the SDK's JSON encoder cannot serialize. In every failure case the entry is still emitted, with only the per-call metadata, and a one-time warning is logged via `System.Logger` at `WARNING` (logger name `ai.auralog.internal`). Subsequent failures from the same SDK instance are silent.
- Backward compatible: absent `globalMetadata`, behaviour is unchanged.

## [0.1.0] - 2026-04-20

### Added
- Initial release.
- `ai.auralog:auralog-core` — static `Auralog` facade, builder config, thread-safe batched HTTP transport, error capture via `Thread.UncaughtExceptionHandler`, automatic JVM shutdown hook.
- `ai.auralog:auralog-slf4j` — SLF4J 2.0+ service provider routing all `org.slf4j` calls to the core facade.
- Full JPMS modules (`ai.auralog.core`, `ai.auralog.slf4j`).
- GraalVM reachability metadata.
