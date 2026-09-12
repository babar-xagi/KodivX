# ADR-0012: Continuous Arrow-Compatible LargeUtf8 Layout for Strings

## Status
Accepted

## Context
Representing text columns as arrays of Kotlin `String` objects introduces massive memory overhead (24-40 bytes of object header and reference per string). For a 5,000,000-row dataset, this creates 5 million heap objects and hundreds of megabytes of GC overhead.

## Decision
KodivX stores string columns using the Arrow LargeUtf8 layout:
1. A single contiguous `ByteArray` holding raw UTF-8 bytes.
2. An `IntArray` of byte offsets demarcating string boundaries.
3. A validity bitmap indicating null strings.
String comparisons, hashing, and dictionary lookups operate directly on raw UTF-8 bytes without instantiating Kotlin `String` objects. Kotlin `String` instances are only constructed when explicitly requested by user-facing view code.

## Consequences
- Up to 80% reduction in memory consumption for string-heavy tables.
- Zero-allocation string comparisons and sorting.
- Zero-copy interchange with Apache Arrow string buffers.
