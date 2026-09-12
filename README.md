# ✨ KodivX

<p align="center">
  <strong>Simple data. Native speed. Everywhere.</strong>
</p>

<p align="center">
  A high-performance, type-safe <strong>Kotlin Multiplatform</strong> analytical computing engine for numerical arrays, columnar DataFrames, and vectorized data queries.
</p>

<p align="center">
  <a href="#-quick-start">Quick Start</a> •
  <a href="#-multiplatform-reach">Multiplatform</a> •
  <a href="#-modules">Modules</a> •
  <a href="#-architecture">Architecture</a> •
  <a href="#-roadmap">Roadmap</a> •
  <a href="CONTRIBUTING.md">Contributing</a>
</p>

---

## 💡 What is KodivX?

**KodivX** gives Kotlin developers a modern data computing experience as approachable as Python's Pandas or Polars, while leveraging Kotlin’s compile-time type safety, zero-overhead memory layouts, native compilation, and multiplatform reach.

```kotlin
import io.kodivx.*

val sales = readCsv("sales.csv")

val summary = sales
    .filter(col<Double>("price") > 100.0)
    .groupBy("country")
    .agg(
        count().alias("total_orders"),
        sum("revenue").alias("gross_revenue"),
        mean("price").alias("avg_price")
    )
    .sortBy(desc("gross_revenue"))

println(summary)
```

Run the exact same analytical logic across **JVM servers, Android apps, iOS devices, native desktop binaries, and WebAssembly in browsers**.

---

## 🌟 Key Pillars

| Design Concept | Inspired By | How KodivX Implements It |
|---|---|---|
| **Vectorized Numerical Arrays** | NumPy | Dense contiguous memory, broadcasting, fast reductions |
| **Columnar DataFrames** | Polars / Arrow | Compact primitive buffers, validity bitmaps, zero cell-level boxing |
| **Lazy Query Optimization** | Polars / DuckDB | Expression ASTs, projection & predicate pushdown, batch execution |
| **Zero-Copy Interoperability** | Apache Arrow | Standardized columnar layout, memory-efficient IPC and exchange |
| **Type-Safe Kotlin DSL** | Kotlin | Extension functions, type inference, sealed types, context parameters |
| **Package Management** | [Qutivex](https://github.com/babar-xagi/Qutivex) / Gradle | Lightning-fast project workflows with `qutivex` and Maven Central |

---

## 🚀 Quick Start

### Using Gradle

Add KodivX to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    // Convenience all-in-one bundle
    implementation("io.kodivx:kodivx:0.1.0")
    
    // Or granular modules
    implementation("io.kodivx:kodivx-array:0.1.0")
    implementation("io.kodivx:kodivx-frame:0.1.0")
}
```

### Using [Qutivex](https://github.com/babar-xagi/Qutivex) (Fast Kotlin Package Manager)

```powershell
# Create a new project and add KodivX instantly
qutivex init my-analytics-app
cd my-analytics-app
qutivex add io.kodivx:kodivx@0.1.0
qutivex run
```

---

## 🛠️ Multiplatform Reach

KodivX is designed multiplatform-first. The portable engine lives in `commonMain` with optional hardware acceleration backends:

```text
commonMain (Portable Engine)
   │
   ├── JVM (Server, Desktop, Android)
   │     └── Optional Vector API & JIT-specialized batch loops
   │
   ├── Kotlin/Native (iOS, macOS, Linux, Windows)
   │     └── Native memory buffers & Apple Accelerate / BLAS sidecars
   │
   ├── Kotlin/Wasm (Modern Web Browsers)
   │     └── Linear memory and Wasm SIMD
   │
   └── Kotlin/JS (Web & Node.js)
         └── JavaScript TypedArrays (Float64Array, Int32Array)
```

---

## 📦 Project Modules

KodivX is organized as an unbundled, lightweight monorepo so that mobile and web applications are never forced to bundle heavy server-only dependencies:

| Module | Description | Targets |
|---|---|---|
| [`:kodivx-core`](modules/core) | Core types (`DataType`, `Schema`, `Field`, errors) | Multiplatform |
| [`:kodivx-buffer`](modules/buffer) | Memory buffers, 64-bit SWAR validity bitmaps, selection vectors | Multiplatform |
| [`:kodivx-array`](modules/array) | 1D/2D numerical vectors, matrices, broadcasting, math kernels | Multiplatform |
| [`:kodivx-frame`](modules/frame) | Columnar DataFrames, series, select, filter, sort, group, join | Multiplatform |
| [`:kodivx-expr`](modules/expr) | Expression AST, type inferencing, predicate simplification | Multiplatform |
| [`:kodivx-io-core`](modules/io) | Multiplatform streaming readers/writers via `kotlinx-io` | Multiplatform |
| [`:kodivx-csv`](modules/csv) | Streaming zero-allocation CSV reader and writer | Multiplatform |
| [`:kodivx-json`](modules/json) | NDJSON and JSON array-of-objects parser | Multiplatform |
| [`:kodivx-parquet`](modules/parquet) | Parquet format reader with row-group pruning | JVM / Native |
| [`:kodivx-arrow`](modules/arrow) | Apache Arrow columnar format import/export | Multiplatform |
| [`:kodivx-sql`](modules/sql) | Embedded SQL parser lowering directly into KodivX plans | Multiplatform |
| [`:kodivx-duckdb`](modules/duckdb) | Optional DuckDB execution and interchange adapter | JVM |

---

## 🏗️ Architecture at a Glance

```text
┌─────────────────────────────────────────────────────────────┐
│                       PUBLIC API                            │
│ array(...) / dataFrameOf(...) / readCsv(...) / col(...)     │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    EXPRESSION AST SYSTEM                    │
│ ColumnRef / Literals / BinaryOp / Cast / Aggregates         │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    LOGICAL QUERY PLAN                       │
│ Scan → Filter → Project → Aggregate → Join → Sort          │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    RULE-BASED OPTIMIZER                     │
│ Projection pushdown / Predicate pushdown / Constant folding │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    VECTORIZED EXECUTION                     │
│ Batch chunks (1024-4096 rows) • SelectionVectors (0-copy)   │
│ 64-bit SWAR Bitmaps • Unrolled specialized primitive loops  │
└─────────────────────────────────────────────────────────────┘
```

---

## 📚 Documentation & Architecture Records

- 📖 [Comprehensive Project Roadmap (KODIVX_ROADMAP.md)](KODIVX_ROADMAP.md)
- 💡 [Architectural Proposals & Recommendations (mysuggestion.md)](mysuggestion.md)
- 🏛️ [Architecture Overview](docs/architecture/overview.md)
- 🚀 [Getting Started Guide](docs/guides/getting-started.md)
- 📊 [Benchmark Policy & Standards](docs/benchmarks/policy.md)
- 📑 [Architecture Decision Records (ADRs)](docs/adr/)
- 🤝 [Contributing Guidelines](CONTRIBUTING.md)
- 🛡️ [Security Policy](SECURITY.md)

---

## 🗺️ Roadmap & Development Phases

KodivX follows an exit-criteria-driven development cycle:

- **Phase 0:** Project Definition & Engineering Foundation *(Current)*
- **Phase 1:** Core Types and Buffer System
- **Phase 2:** Array and Numerical Core
- **Phase 3:** Columnar DataFrame MVP
- **Phase 4:** GroupBy, Joins, and Statistics
- **Phase 5:** Expression Engine & Type Checking
- **Phase 6:** Lazy Query Engine & Plan Diagnostics
- **Phase 7:** Optimizer & Vectorized Batch Execution
- **Phase 8:** CSV & JSON I/O
- **Phase 9:** Cross-Platform Hardening (Android, iOS, Wasm, JS, Desktop)
- **Phase 10:** Parquet Support
- **Phase 11:** Apache Arrow Interoperability
- **Phase 12:** Embedded SQL Layer
- **Phase 13:** JVM / Server Integrations (DuckDB, JDBC)
- **Phase 14:** Native Performance Acceleration (SIMD, Apple Accelerate)
- **Phase 15:** Typed Schema Experience
- **Phase 16:** Stable 1.0 Release

---

## ⚖️ License

KodivX is open-source software licensed under the [Apache License, Version 2.0](LICENSE).
