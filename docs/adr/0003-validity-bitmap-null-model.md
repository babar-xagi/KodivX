# ADR-0003: Validity Bitmap Null Representation

## Status
Accepted

## Context
Storing nullable primitive columns using Kotlin's boxed nullable types (e.g. `Array<Double?>` or `List<Int?>`) forces primitive numbers into heap wrapper objects. This creates an 8x-16x memory footprint penalty and constant garbage collection pauses.

## Decision
KodivX adopts the Apache Arrow validity bitmap specification. Every column maintains:
1. A dense, contiguous value buffer containing raw primitive data.
2. A compact validity bitmap where bit `1` indicates a valid value and bit `0` indicates `null`.
Bitmaps are stored as `LongArray` words, enabling 64-bit word scanning and hardware-accelerated population counts (`Long.countOneBits()`).

## Consequences
- Nullable columns consume minimal extra memory (1 bit per cell).
- Inner execution loops can skip null checks entirely when 64-bit words evaluate to `-1L` (all valid).
- Direct zero-copy compatibility with the Apache Arrow columnar standard.
