# ADR-0001: Columnar Storage as Primary Table Representation

## Status
Accepted

## Context
Traditional object-oriented data structures store collections of row objects (e.g. `List<UserRow>`). In analytical workloads, queries typically filter or aggregate across a handful of columns across millions of records. Row-oriented storage wastes memory bandwidth by pulling unused attributes into CPU caches.

## Decision
KodivX adopts a column-oriented storage layout as its primary physical representation for `DataFrame`. Each column is stored as an independent, contiguous sequence of values.

## Consequences
- Significant reduction in memory bandwidth consumption during analytical queries.
- CPU cache lines are saturated with homogenous data, enabling autovectorization and SIMD.
- Row-level access becomes a view over columns rather than the primary storage format.
