# ADR-0002: Immutable Public DataFrame Operations

## Status
Accepted

## Context
Mutable data frames in multithreaded or functional pipelines cause subtle race conditions and unexpected side effects. Conversely, eager deep copying on every transformation wastes memory and CPU cycles.

## Decision
All public DataFrame and Column transformation methods (`filter`, `select`, `withColumn`, `sortBy`) return logically immutable instances. Internal builders and physical execution chunks may use mutable buffers during construction, but published frames are immutable. Slices and projections reuse underlying buffers without copying data wherever possible.

## Consequences
- Thread-safe querying and sharing of DataFrame instances across coroutines and threads.
- Predictable functional transformations adhering to Kotlin idioms.
- Zero copy overhead achieved via buffer views and selection vectors.
