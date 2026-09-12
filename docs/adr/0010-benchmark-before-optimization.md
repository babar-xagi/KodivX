# ADR-0010: Benchmark-Before-Optimization Policy

## Status
Accepted

## Context
Premature micro-optimizations often add maintenance complexity, obscure bugs, and degrade portability without delivering measurable real-world speedups.

## Decision
No optimization or performance rewrite may be merged into the repository without an accompanying reproducible benchmark demonstrating:
1. The baseline performance bottleneck.
2. The measured performance gain under controlled, repeatable conditions.
3. Verification that output semantics and correctness remain 100% identical.

## Consequences
- Protects codebase simplicity and long-term maintainability.
- Ensures all performance claims published by KodivX are verifiable and trustworthy.
