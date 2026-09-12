# ADR-0013: Adopt kotlinx-io as the Multiplatform Stream Contract

## Status
Accepted

## Context
Multiplatform I/O across JVM, Android, Native (Linux, macOS, Windows, iOS), JS, and Wasm has historically required custom platform abstraction layers or fragmented implementations.

## Decision
KodivX standardizes on JetBrains' official `kotlinx-io` (`kotlinx-io-core`) as the foundational streaming abstraction (`Source` / `Sink`) for file and network data parsing (CSV, JSON, Arrow IPC, Parquet).

## Consequences
- Single, consistent, and highly-optimized streaming I/O implementation across all Kotlin Multiplatform targets.
- Endian-aware binary reading and zero-copy segment slicing out-of-the-box.
- Direct backing by JetBrains official multiplatform engineering.
