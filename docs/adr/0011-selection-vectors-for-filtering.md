# ADR-0011: Selection Vectors for Intermediate Relational Operations

## Status
Accepted

## Context
In standard naive query engines, applying a filter (`df.filter(price > 100)`) immediately allocates new primitive arrays and copies matching rows. When operations are chained (`filter(A).filter(B).project(C)`), this causes repeated memory allocations, memory bandwidth saturation, and heavy garbage collection overhead.

## Decision
Physical execution chunks carry an optional `SelectionVector` (`IntArray` of valid row indices). Filtering a chunk populates a reusable thread-local selection vector without copying column data buffers. Downstream projection, aggregation, and filter operators iterate over indices in the selection vector. Data is only materialized into contiguous buffers at pipeline termination boundaries.

## Consequences
- Near-zero intermediate memory allocation during chained filter and projection pipelines.
- Drastic reduction in garbage collection pauses on mobile (Android/iOS) and WebAssembly runtimes.
- Minimal indirection cost in tight kernel loops when processing sparse filter matches.
