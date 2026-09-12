# ADR-0009: Modular Artifact Boundaries

## Status
Accepted

## Context
Data applications have widely varying requirements: an iOS or Android application may only need basic arrays and CSV parsing, whereas a JVM server may require full Parquet, Arrow, SQL, and DuckDB integration. A single monolithic artifact would result in bloated mobile app packages.

## Decision
KodivX is published as modular artifacts:
- `kodivx-core`
- `kodivx-buffer`
- `kodivx-array`
- `kodivx-frame`
- `kodivx-expr`
- `kodivx-io-core`
- `kodivx-csv`
- `kodivx-json`
- `kodivx-parquet`
- `kodivx-arrow`
- `kodivx-sql`
- `kodivx-duckdb`
A convenience meta-module `kodivx` bundles common components for users who prefer a single dependency.

## Consequences
- Fine-grained control over dependencies and binary size for Android, iOS, and WebAssembly targets.
- Clear module boundaries enforce architectural layering and prevent cyclical dependencies.
