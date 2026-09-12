# ADR-0005: Optional Platform Acceleration Backends

## Status
Accepted

## Context
Platforms possess unique hardware acceleration capabilities:
- JVM can leverage Project Panama Vector API and JIT unrolling.
- Apple platforms (macOS/iOS) have Apple Accelerate (BLAS/vDSP).
- WebAssembly has Wasm SIMD.
However, hardcoding native or platform-specific libraries into the core module breaks portability and creates distribution nightmares.

## Decision
KodivX provides a correct, highly-optimized portable Kotlin implementation by default. Platform accelerators (such as BLAS or Apple Accelerate) are pluggable sidecars discovered at runtime or explicitly configured.

## Consequences
- The core library runs anywhere without native binaries or dynamic libraries.
- Platforms with acceleration hardware can opt in for maximum performance.
- Testing verifies that accelerated backends produce identical results to reference portable kernels.
