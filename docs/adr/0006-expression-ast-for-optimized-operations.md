# ADR-0006: Expression AST for Optimized Operations

## Status
Accepted

## Context
Filtering or mutating data via arbitrary user lambdas (e.g. `df.filter { row -> row["price"] > 100 }`) turns the execution into an opaque black box. The query engine cannot inspect, optimize, vectorize, or push down predicates into Parquet or database scans.

## Decision
KodivX uses a typed Abstract Syntax Tree (AST) for expressions (`col("price") > 100`). Arbitrary lambdas are supported as an escape hatch, but expressions are the primary recommendation for high-performance pipelines.

## Consequences
- Expressions can be type-checked and validated before execution.
- Optimizers can apply constant folding, boolean simplification, and predicate pushdown.
- Physical execution engines can execute vectorized batch kernels rather than row-by-row function calls.
