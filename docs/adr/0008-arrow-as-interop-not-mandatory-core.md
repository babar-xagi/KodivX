# ADR-0008: Arrow as Interoperability Layer, Not Mandatory Core Runtime

## Status
Accepted

## Context
Apache Arrow is the gold standard for columnar data exchange in memory. However, bundling the full Apache Arrow Java/C++ runtime into Kotlin Multiplatform imposes heavy binary bloat, native dependency hurdles, and compatibility issues on mobile/Wasm targets.

## Decision
KodivX aligns its internal physical layouts (validity bitmaps, contiguous buffers, offset-based strings) with Arrow specifications, but implements them in pure, lightweight Kotlin. The official Apache Arrow runtime is integrated via an optional interop module (`kodivx-arrow`) rather than a mandatory core dependency.

## Consequences
- Lightweight core library for client-side, mobile, and web applications.
- Zero-copy or near-zero-copy conversion to and from Apache Arrow record batches when the interop module is included.
- Freedom to optimize internal structures for Kotlin Multiplatform without external runtime constraints.
