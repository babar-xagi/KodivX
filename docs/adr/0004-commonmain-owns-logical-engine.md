# ADR-0004: commonMain Owns the Logical Engine

## Status
Accepted

## Context
Many existing Kotlin data libraries rely on JVM-specific dependencies (such as `java.nio`, reflection, or Java streams), making multiplatform compilation impossible or brittle.

## Decision
All core types (`DataType`, `Schema`, `Field`), data structures (`Column`, `DataFrame`, `Vector`, `Matrix`), expression ASTs, logical query plans, rule optimizers, and reference execution kernels must reside strictly in `commonMain` without depending on JVM or platform-specific packages.

## Consequences
- 100% portable core that compiles natively for iOS, Android, macOS, Linux, Windows, JS, and WebAssembly.
- Consistent semantic behavior across all target runtimes.
- Platform-specific acceleration is isolated in optional sidecars or platform source sets.
