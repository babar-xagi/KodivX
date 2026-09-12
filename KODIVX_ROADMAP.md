# KodivX — Comprehensive Project Roadmap

> **Working tagline:** *Simple data. Native speed. Everywhere.*
>
> **Project type:** Kotlin Multiplatform numerical + tabular analytical computing engine  
> **Primary language:** Kotlin  
> **Targets:** JVM/server, Android, iOS/Kotlin Native, desktop, Kotlin/JS, Kotlin/Wasm  
> **Document status:** Project blueprint / roadmap  
> **Last updated:** September 2026

---

## 1. Executive Summary

**KodivX** is a proposed Kotlin Multiplatform data and analytical computing ecosystem designed around one idea:

> Give Kotlin developers a data experience that is as approachable as Python, while preserving Kotlin's type safety, native deployment, multiplatform reach, and potential for high-performance execution.

KodivX should **not** be a literal port of NumPy, pandas, Polars, DuckDB, or PyArrow. It should learn from the strongest ideas in each:

| Inspiration | Idea KodivX should learn from |
|---|---|
| NumPy | Dense arrays, vectorized numerical operations, broadcasting |
| pandas | Simple exploratory tabular API and rich data manipulation |
| Polars | Columnar execution, expressions, lazy plans, query optimization |
| DuckDB | Embedded analytics, SQL-friendly querying, vectorized execution |
| Apache Arrow | Standardized columnar memory and zero/low-copy interchange |
| Kotlin | Static typing, DSLs, extension functions, multiplatform compilation |

KodivX should expose a **small, coherent public API** while internally using a modular architecture.

The long-term result should feel like one library:

```kotlin
import io.kodivx.*

val sales = readCsv("sales.csv")

val result = sales
    .filter { col("price") > 100 }
    .groupBy("country")
    .agg(mean("price").alias("avg_price"))
    .sortBy(desc("avg_price"))

println(result)
```

The same conceptual API should work on server, desktop, Android, iOS, and web wherever the operation and platform capabilities allow it.

---

# 2. Project Vision

## Vision Statement

**KodivX will become a fast, lightweight, type-safe, multiplatform analytical computing foundation for Kotlin.**

A developer should be able to learn one API for:

- arrays and vectors;
- matrices and numerical operations;
- DataFrames and typed columns;
- filtering, selecting, sorting, joining and grouping;
- statistics;
- CSV and JSON;
- Parquet and Arrow interoperability;
- lazy analytical queries;
- SQL;
- local analytical execution;
- mobile and edge analytics;
- browser-side analytics;
- AI/ML data preprocessing.

The developer should **not need to understand the execution engine** to use the library.

---

# 3. Core Project Goal

The primary goal is:

> **Build one simple Kotlin API for numerical and tabular data that can execute efficiently across Kotlin Multiplatform targets.**

This can be broken into seven major goals.

## Goal 1 — Python-like simplicity

Common operations should be discoverable and concise.

Bad:

```kotlin
val result = SomeDataFrameExecutionFactory
    .newExecutionContext(DefaultAllocator())
    .createDataFrame(...)
```

Desired:

```kotlin
val df = dataFrameOf(
    "name" to listOf("Ali", "Sara"),
    "age" to listOf(21, 24)
)

df.filter { col("age") >= 18 }
```

The API should reduce ceremony without pretending Kotlin is Python.

---

## Goal 2 — Kotlin-first design

KodivX should embrace Kotlin rather than imitate Python syntax exactly.

Use:

- null safety;
- generics;
- extension functions;
- inline functions where appropriate;
- operators only when their meaning is obvious;
- DSLs;
- sealed types;
- value classes where useful;
- coroutines for asynchronous I/O, not as a substitute for CPU parallelism;
- compiler assistance later for typed schema access.

Example:

```kotlin
val adults = people.filter {
    col<Int>("age") >= 18
}
```

Later, a typed schema API may allow:

```kotlin
val adults = people.filter {
    age >= 18
}
```

---

## Goal 3 — Multiplatform by architecture, not as an afterthought

The portable core must live in `commonMain`.

Target direction:

```text
commonMain
   |
   +-- JVM
   |    +-- server
   |    +-- desktop
   |    +-- Android
   |
   +-- Kotlin/Native
   |    +-- iOS
   |    +-- macOS
   |    +-- Linux
   |    +-- Windows where supported
   |
   +-- Kotlin/JS
   |
   +-- Kotlin/Wasm
```

Platform-specific acceleration should be optional.

A platform that cannot use an optimized backend must still have a correct portable implementation whenever practical.

---

## Goal 4 — Performance by design

Performance must come from architecture rather than scattered micro-optimizations.

Core principles:

- columnar storage for tabular data;
- primitive arrays/buffers rather than boxed objects for primitive columns;
- validity bitmaps for missing values;
- contiguous memory where possible;
- expression execution rather than row-by-row callbacks in performance-sensitive paths;
- batch/vector execution;
- minimal intermediate allocation;
- lazy execution where it materially improves performance;
- projection and predicate pushdown;
- streaming where full materialization is unnecessary;
- optional SIMD/native acceleration;
- benchmark every optimization.

KodivX must **never claim to be faster than pandas, Polars, DuckDB, NumPy or another system without reproducible benchmarks**.

---

## Goal 5 — Small core, modular ecosystem

"One library" should mean **one coherent ecosystem**, not one giant artifact.

A mobile application that needs arrays and CSV should not be forced to bundle SQL, Parquet, Arrow and database integrations.

Proposed modules:

```text
kodivx
├── kodivx-core
├── kodivx-array
├── kodivx-frame
├── kodivx-expr
├── kodivx-stats
├── kodivx-io
├── kodivx-csv
├── kodivx-json
├── kodivx-parquet
├── kodivx-arrow
├── kodivx-sql
├── kodivx-duckdb
├── kodivx-linalg
├── kodivx-benchmarks
└── kodivx-bom
```

A convenience dependency can expose the normal experience:

```kotlin
implementation("io.kodivx:kodivx:<version>")
```

Advanced users can depend only on required modules.

---

## Goal 6 — Interoperability rather than ecosystem isolation

KodivX should cooperate with existing technologies.

Priority interoperability:

1. Kotlin collections
2. primitive Kotlin arrays
3. Java arrays and collections on JVM
4. CSV/JSON
5. Parquet
6. Apache Arrow
7. DuckDB on supported targets
8. JDBC on JVM
9. NumPy-compatible exchange where technically sensible
10. ML tensors / model runtimes later

The project should avoid inventing proprietary data formats unless there is a compelling reason.

---

## Goal 7 — Excellent developer experience

Performance alone will not make KodivX successful.

KodivX should provide:

- clear error messages;
- useful `toString()` table rendering;
- IDE autocomplete;
- API documentation;
- cookbook examples;
- Kotlin Notebook support;
- sample Android/iOS/web apps;
- deterministic behavior;
- stable naming conventions;
- migration guides;
- benchmark transparency.

---

# 4. Non-Goals for Version 1.x

The following should **not** block the first stable release:

- replacing Apache Spark;
- distributed multi-machine execution;
- implementing a complete database management system;
- implementing the entire SQL standard;
- GPU compute on every platform;
- automatic machine learning;
- deep learning model training;
- exact pandas API compatibility;
- exact NumPy API compatibility;
- copying Polars internals;
- building a complete visualization framework;
- cloud data warehouse integrations;
- custom distributed scheduler;
- custom object storage system.

These can become integrations or future projects.

Keeping these outside v1 is essential to avoid an endless project.

---

# 5. Product Positioning

KodivX should occupy this position:

```text
                       EASY TO USE
                           ↑
                           |
                       KodivX
                          /|
                         / |
                 pandas /  | Kotlin DataFrame
                       /   |
                      /    |
                     /     |
           Polars --/------|----------------→ PERFORMANCE
                  /        |
                 /         |
             NumPy         |
                           |
                  Kotlin Multiplatform
                 + native/mobile/web reach
```

This is conceptual positioning, not a benchmark claim.

## Short description

> **KodivX is a Kotlin Multiplatform analytical computing library for arrays, DataFrames and fast local data processing.**

## Longer description

> **KodivX combines numerical arrays, columnar DataFrames, expressions, lazy analytical execution and interoperable data formats behind a small Kotlin-first API that can run across server, desktop, mobile and web targets.**

---

# 6. Design Principles

Every major API or architecture decision should be checked against these principles.

### P1. Simple things must be simple

```kotlin
df.mean("price")
```

should not require an execution context, allocator or schema object from the user.

### P2. Advanced things must remain possible

Low-level buffer, schema, expression and execution APIs should exist for advanced users without polluting the beginner API.

### P3. Correctness before speed

A fast incorrect result is useless.

### P4. Measure before optimizing

No performance rewrite without a benchmark demonstrating the problem.

### P5. Avoid boxing primitive data

Primitive columns should be backed by primitive or equivalent compact buffers.

### P6. Columnar by default for analytical tables

Row objects can exist as a view/API, but the primary analytical storage model should remain column-oriented.

### P7. Immutable results by default

Operations should normally return new logical values/views.

For high-throughput loading, provide builders and mutable internal buffers.

### P8. Multiplatform behavior must be predictable

Platform-specific implementations may differ internally, but public semantics should remain consistent.

### P9. Dependencies must earn their weight

Particularly important for Android, iOS and web bundles.

### P10. Interop is a feature

Arrow, Parquet and database integration should strengthen KodivX rather than be treated as competitors.

---

# 7. Proposed User Experience

## 7.1 Arrays

```kotlin
val x = array(1.0, 2.0, 3.0, 4.0)

println(x.sum())
println(x.mean())

val matrix = x.reshape(2, 2)
```

Potential numerical expressions:

```kotlin
val y = (x * 2.0) + 1.0
```

---

## 7.2 DataFrames

```kotlin
val users = dataFrameOf(
    "name" to listOf("Ali", "Sara", "Babar"),
    "age" to intArrayOf(21, 24, 23),
    "score" to doubleArrayOf(81.0, 95.0, 90.0)
)
```

Operations:

```kotlin
val top = users
    .filter { col<Double>("score") >= 90.0 }
    .select("name", "score")
    .sortBy(desc("score"))
```

---

## 7.3 Reading data

```kotlin
val df = readCsv("sales.csv")
```

or:

```kotlin
val df = read.csv("sales.csv")
```

The project should select **one style** after API experimentation instead of permanently maintaining many aliases.

---

## 7.4 Expressions

Prefer an expression representation for optimized operations:

```kotlin
val result = df.filter(
    col<Int>("age") >= 18
)
```

Expressions can be inspected and optimized.

By contrast, arbitrary lambdas such as this are harder to optimize:

```kotlin
df.filterRows { row ->
    customFunction(row)
}
```

Both may exist, but documentation should guide users toward expressions for high-performance pipelines.

---

## 7.5 Grouping and aggregation

```kotlin
val result = sales
    .groupBy("country")
    .agg(
        sum("revenue"),
        mean("revenue").alias("avg_revenue"),
        count().alias("orders")
    )
```

---

## 7.6 Joins

```kotlin
val result = orders.join(
    customers,
    on = "customer_id",
    how = JoinType.Left
)
```

---

## 7.7 Lazy API

```kotlin
val query = scanParquet("sales/*.parquet")
    .filter(col<Double>("price") > 100.0)
    .select("country", "price")
    .groupBy("country")
    .agg(mean("price"))
```

Nothing expensive should execute until:

```kotlin
val result = query.collect()
```

Debugging:

```kotlin
println(query.explain())
```

Possible output:

```text
Aggregate [country, mean(price)]
  Projection [country, price]
    Filter [price > 100]
      ParquetScan [sales/*.parquet]
```

After optimization:

```text
Aggregate [country, mean(price)]
  ParquetScan
    columns = [country, price]
    predicate = price > 100
```

---

# 8. High-Level Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                       PUBLIC API                            │
│ array / frame / read / scan / col / stats / sql           │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    LOGICAL DATA MODEL                       │
│ DataType / Schema / Array / Column / Frame / Scalar        │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    EXPRESSION SYSTEM                        │
│ literals / columns / arithmetic / boolean / aggregate      │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    LOGICAL QUERY PLAN                       │
│ scan / filter / project / join / group / sort / aggregate  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                       OPTIMIZER                             │
│ projection pushdown / predicate pushdown / folding / etc.  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     PHYSICAL PLANNER                        │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    EXECUTION ENGINE                         │
│ vector kernels / hash aggregate / joins / sort / streaming │
└──────────────┬────────────────┬────────────────┬────────────┘
               │                │                │
          Portable Kotlin      JVM            Native/Web
               │                │                │
               └────────────────┴────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                       I/O / INTEROP                         │
│ CSV / JSON / Parquet / Arrow / DB / DuckDB / streams      │
└─────────────────────────────────────────────────────────────┘
```

---

# 9. Core Data Model

A clear data model should be designed before building a large API.

## 9.1 Scalar types

Initial stable core:

```text
Boolean
Int
Long
Float
Double
String
Binary
```

Then add:

```text
Byte
Short
Unsigned integer variants if justified
Decimal
Date
Time
DateTime / Timestamp
Duration
Categorical
List
Struct
Map where useful
```

Do not add every possible type to MVP.

---

## 9.2 Null model

Avoid storing primitive nullable values as boxed Kotlin objects such as:

```kotlin
Array<Double?>
```

for the primary column representation.

Prefer:

```text
Values buffer
+ validity bitmap
```

Conceptually:

```text
values:   [10.5][22.0][??][18.4]
validity:    1     1    0    1
```

This is compact and aligns well with modern columnar systems.

---

## 9.3 Column representation

Concept:

```kotlin
interface Column<T> {
    val name: String
    val type: DataType
    val size: Int

    operator fun get(index: Int): T?
}
```

Primitive implementations should use specialized storage:

```text
IntColumn       -> IntArray / native buffer
LongColumn      -> LongArray / native buffer
DoubleColumn    -> DoubleArray / native buffer
BooleanColumn   -> bit/byte representation
StringColumn    -> offsets + UTF-8 bytes
```

The public abstraction must not force every value through `Any?`.

---

## 9.4 DataFrame representation

Conceptually:

```text
DataFrame
├── Schema
├── Column 0
├── Column 1
├── Column 2
└── rowCount
```

All columns must have compatible row counts.

Rows should generally be **views over columns**, not the physical storage format.

---

# 10. Array / NDArray Architecture

KodivX arrays should solve numerical computing without turning the first release into a full scientific Python clone.

## MVP

- 1D arrays
- 2D arrays/matrices
- shape
- indexing
- slicing
- reshape
- basic broadcasting
- arithmetic
- reductions
- dot product
- transpose
- descriptive statistics

Later:

- N-dimensional generalization
- advanced broadcasting
- matrix decompositions
- BLAS integration
- sparse matrices
- FFT
- specialized tensor operations

Possible API:

```kotlin
val a = array(1.0, 2.0, 3.0)
val b = array(10.0, 20.0, 30.0)

val c = a + b

println(c.mean())
```

---

# 11. Expression Engine

The expression engine will eventually be one of KodivX's most important components.

## Why expressions?

This:

```kotlin
filter(col<Int>("age") > 18)
```

can become an AST:

```text
GreaterThan
├── Column(age)
└── Literal(18)
```

KodivX can then:

- infer types;
- validate before execution;
- combine operations;
- fold constants;
- push predicates into scans;
- execute operations in batches;
- compile specialized kernels later.

## Initial expression nodes

```text
Literal
ColumnRef
Alias
Not
And
Or
Equal
NotEqual
GreaterThan
GreaterOrEqual
LessThan
LessOrEqual
Add
Subtract
Multiply
Divide
IsNull
IsNotNull
Cast
```

Aggregation:

```text
Count
Sum
Min
Max
Mean
Variance
StdDev
```

String expressions later:

```text
Contains
StartsWith
EndsWith
Lower
Upper
Length
Regex
```

---

# 12. Query Plan

## Logical nodes

```text
Scan
Projection
Filter
Limit
Sort
Aggregate
Distinct
Join
Union
```

Later:

```text
Window
Explode
Pivot
Unpivot
```

Every logical plan should be printable.

Example:

```kotlin
query.explain()
```

This is important for debugging, teaching and performance analysis.

---

# 13. Query Optimizer Roadmap

Do not begin with a complicated cost-based optimizer.

Start with deterministic rule-based optimization.

## Stage A

- constant folding;
- boolean simplification;
- remove redundant projections;
- combine adjacent filters;
- combine adjacent projections.

## Stage B

- projection pushdown;
- predicate pushdown;
- limit pushdown;
- null simplification.

## Stage C

- common subexpression reduction where useful;
- join-side projection pruning;
- filter propagation;
- expression rewriting.

## Stage D

Only if real workloads justify it:

- statistics collection;
- cardinality estimation;
- join ordering;
- cost-based optimization.

---

# 14. Execution Engine

## Execution model

Prefer batches/chunks:

```text
Source
  ↓
Batch 1 ──→ Filter ──→ Projection ──→ Aggregate
Batch 2 ──→ Filter ──→ Projection ──→ Aggregate
Batch 3 ──→ Filter ──→ Projection ──→ Aggregate
```

Benefits:

- lower peak memory;
- cache-friendly processing;
- future SIMD opportunities;
- better streaming;
- parallelism.

## Initial physical operators

- memory scan;
- CSV scan;
- filter;
- project;
- limit;
- sort;
- hash aggregate;
- hash join;
- materialize.

Later:

- Parquet scan;
- merge join;
- streaming aggregate;
- partitioned hash join;
- external sort/spill.

---

# 15. Memory Architecture

Performance and cross-platform behavior will depend heavily on this layer.

## Requirements

- compact primitive buffers;
- predictable lifetime;
- safe public APIs;
- slicing without unnecessary copying;
- immutable logical views;
- optional owned/mutable builders;
- controlled platform-specific optimization;
- clear ownership for native resources.

## Proposed abstraction

```text
Buffer
├── HeapBuffer
├── PrimitiveBuffer
├── NativeBuffer
├── ArrowBufferAdapter
└── WasmBuffer
```

Do **not** expose backend-specific memory details in beginner APIs.

---

# 16. Multiplatform Architecture

## commonMain must contain

- DataType
- Schema
- scalar model
- arrays
- columns
- DataFrame
- expressions
- logical plans
- portable execution kernels
- core statistics
- most CSV/JSON parsing logic where practical

## Platform-specific code should contain only what needs it

### JVM

Potential acceleration/integration:

- JIT-friendly loops;
- Java Vector API experimentation;
- NIO buffers;
- JDBC;
- Apache Arrow Java integration;
- DuckDB JDBC integration;
- BLAS/native bindings where appropriate.

### Android

Priorities:

- small artifact size;
- controlled allocations;
- startup time;
- no mandatory desktop/JVM-only dependencies;
- device memory awareness.

### Kotlin/Native / iOS

Priorities:

- native buffer ownership;
- interoperability with platform numerical libraries where useful;
- Apple Accelerate integration as an optional accelerator;
- predictable threading.

### Kotlin/JS

Priorities:

- typed arrays;
- browser File/Blob integration;
- streaming parsing where available;
- avoid huge object graphs.

### Kotlin/Wasm

Priorities:

- linear-memory-friendly buffers;
- Wasm-compatible kernels;
- feature detection for evolving SIMD/thread capabilities;
- keep portable fallback behavior.

---

# 17. I/O Roadmap

## Tier 1 — Must-have

### CSV

Features:

- headers;
- delimiter configuration;
- quote handling;
- escaped quotes;
- UTF-8;
- null tokens;
- schema inference;
- explicit schema;
- streaming/chunk reading.

Example:

```kotlin
val df = readCsv(
    "users.csv",
    schema = schema {
        int("id")
        string("name")
        double("score")
    }
)
```

### JSON

Start with:

- array-of-objects;
- JSON Lines / NDJSON.

Avoid trying to normalize arbitrary nested JSON automatically in the first version.

---

## Tier 2 — Analytical formats

### Parquet

Needed capabilities:

- column projection;
- type conversion;
- row-group reading;
- predicate pushdown where supported;
- metadata inspection.

### Arrow

Treat Arrow primarily as an **interchange contract**.

Goals:

- import Arrow arrays/batches;
- export KodivX columns/frames;
- minimize copies where layouts are compatible;
- preserve null masks and types correctly.

---

# 18. SQL Roadmap

SQL should sit **on top of the same logical plan**, not become a completely separate execution system.

```text
Kotlin DataFrame DSL ──┐
                       ├──→ Logical Plan → Optimizer → Execution
SQL Parser ────────────┘
```

This is critical.

If DSL and SQL become separate engines, maintenance doubles.

## SQL MVP

```sql
SELECT
FROM
WHERE
LIMIT
ORDER BY
GROUP BY
basic aggregates
INNER JOIN
LEFT JOIN
```

Later:

- CTEs;
- window functions;
- subqueries;
- richer joins;
- SQL functions;
- set operations.

Do not attempt full SQL compatibility in the first stable release.

---

# 19. DuckDB Integration Strategy

DuckDB should initially be treated as an **optional integration/backend**, especially on JVM/server environments.

Potential uses:

```kotlin
val result = duckDb.query("""
    SELECT country, avg(price)
    FROM sales
    GROUP BY country
""")
```

or conversion:

```kotlin
val frame = result.toKodivX()
```

Long term, KodivX SQL should still have its own portable local execution capability where appropriate.

This prevents KodivX from becoming only a wrapper around a JVM-native database.

---

# 20. Apache Arrow Strategy

Arrow is highly relevant because it defines a language-independent columnar memory layout designed for analytical processing and efficient interchange.

KodivX should **align with Arrow concepts where they are good fits**, without forcing the entire portable core to depend on a specific Arrow runtime implementation.

Architecture:

```text
KodivX logical types
        │
        ▼
KodivX buffers
        │
        ├── portable implementation
        │
        └── Arrow adapters
                │
                ├── JVM Arrow
                └── C Data Interface / native bridge later
```

This allows KodivX to remain lightweight while gaining ecosystem interoperability.

---

# 21. Public Module Proposal

## `kodivx-core`

Contains:

- basic types;
- schema;
- buffer abstractions;
- errors;
- configuration;
- core utilities.

Must remain tiny.

---

## `kodivx-array`

Contains:

- Vector;
- Matrix;
- NDArray later;
- slicing;
- reshape;
- arithmetic;
- reductions.

---

## `kodivx-frame`

Contains:

- Column;
- Series if the concept is retained;
- DataFrame;
- schema operations;
- row views;
- joins;
- group API.

---

## `kodivx-expr`

Contains:

- expression AST;
- type checking;
- expression simplification;
- scalar kernels.

---

## `kodivx-query`

Contains:

- logical plans;
- optimizer;
- physical planner;
- execution operators.

Potentially merge this into `kodivx-frame` until the architecture is large enough to justify separation.

---

## `kodivx-stats`

Contains:

- descriptive stats;
- covariance/correlation;
- quantiles;
- selected statistical functions.

---

## `kodivx-csv`

CSV reader/writer.

---

## `kodivx-json`

JSON / NDJSON reader/writer.

---

## `kodivx-parquet`

Parquet support.

---

## `kodivx-arrow`

Arrow interoperability.

---

## `kodivx-sql`

SQL parser/planner.

---

## `kodivx-duckdb`

Optional DuckDB integration.

---

## `kodivx-linalg`

Advanced matrix operations and optional BLAS backends.

---

## `kodivx-benchmarks`

Never published as a normal application dependency.

Includes reproducible benchmark workloads.

---

# 22. Repository Structure

Suggested monorepo:

```text
kodivx/
├── README.md
├── ROADMAP.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── LICENSE
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── docs/
│   ├── architecture/
│   ├── guides/
│   ├── design-decisions/
│   └── benchmarks/
├── modules/
│   ├── core/
│   ├── array/
│   ├── frame/
│   ├── expr/
│   ├── query/
│   ├── stats/
│   ├── csv/
│   ├── json/
│   ├── parquet/
│   ├── arrow/
│   ├── sql/
│   ├── duckdb/
│   └── linalg/
├── samples/
│   ├── jvm-cli/
│   ├── android/
│   ├── ios/
│   ├── desktop/
│   ├── js-browser/
│   └── wasm-browser/
├── benchmarks/
│   ├── jmh/
│   ├── datasets/
│   └── reports/
└── integration-tests/
```

---

# 23. Development Phases

The phases below are dependency-ordered. Do not move to a phase merely because a calendar date arrives. Move when the **exit criteria** are satisfied.

---

# Phase 0 — Project Definition and Engineering Foundation

## Objective

Turn the idea into a disciplined open-source engineering project.

## Deliverables

- [ ] Confirm project name `KodivX`
- [ ] Search Maven Central / GitHub / package registries for naming conflicts
- [ ] Decide group ID, e.g. `io.kodivx`
- [ ] Select license
- [ ] Create Git repository
- [ ] Add CI
- [ ] Add formatting/linting
- [ ] Configure Kotlin Multiplatform
- [ ] Establish minimum supported platforms
- [ ] Create architecture decision record system
- [ ] Set semantic versioning policy
- [ ] Create benchmark policy
- [ ] Create contribution guidelines
- [ ] Add API compatibility checking

## Key documents

```text
README.md
ROADMAP.md
CONTRIBUTING.md
ARCHITECTURE.md
BENCHMARKS.md
```

## Exit criteria

A minimal `kodivx-core` project compiles and tests on the initially supported targets with automated CI.

---

# Phase 1 — Core Types and Buffer System

## Objective

Build the foundation everything else uses.

## Features

### Data types

- [ ] Boolean
- [ ] Int
- [ ] Long
- [ ] Float
- [ ] Double
- [ ] String
- [ ] Binary
- [ ] Null semantics

### Core abstractions

- [ ] `DataType`
- [ ] `Field`
- [ ] `Schema`
- [ ] `Scalar`
- [ ] `Buffer`
- [ ] primitive specialized buffers
- [ ] validity bitmap
- [ ] slicing/view semantics

### Builders

- [ ] Int builder
- [ ] Long builder
- [ ] Double builder
- [ ] String builder
- [ ] automatic capacity growth

## Critical tests

- null correctness;
- slice correctness;
- boundary checks;
- builder growth;
- memory ownership;
- platform consistency;
- random/property-based tests.

## Exit criteria

Millions of primitive values can be stored and scanned without allocating one Kotlin object per scalar value.

---

# Phase 2 — Array and Numerical Core

## Objective

Create the first useful end-user API.

## API

```kotlin
val x = array(1.0, 2.0, 3.0)

x.sum()
x.mean()
x.min()
x.max()
```

## Features

- [ ] primitive vectors
- [ ] shape
- [ ] indexing
- [ ] slicing
- [ ] map
- [ ] zip
- [ ] arithmetic
- [ ] reductions
- [ ] reshape
- [ ] 2D matrix
- [ ] transpose
- [ ] dot product
- [ ] basic broadcasting

## Performance work

Benchmark:

- scalar loop;
- vector addition;
- sum;
- mean;
- dot product;
- slicing.

Baselines where relevant:

- plain Kotlin arrays;
- Multik;
- JVM numerical alternatives;
- NumPy benchmark results only when executed under a clearly documented environment.

## Exit criteria

KodivX arrays are useful independently of DataFrames and have stable semantics across supported platforms.

---

# Phase 3 — DataFrame MVP

## Objective

Deliver a usable columnar DataFrame.

## Features

- [ ] `Column<T>`
- [ ] primitive specialized columns
- [ ] string column
- [ ] `DataFrame`
- [ ] schema
- [ ] row count
- [ ] column access
- [ ] column creation
- [ ] select
- [ ] rename
- [ ] drop
- [ ] filter
- [ ] sort
- [ ] limit/head/tail
- [ ] null handling
- [ ] simple aggregate functions
- [ ] readable console rendering

Example:

```kotlin
val df = dataFrameOf(
    "name" to listOf("A", "B", "C"),
    "score" to doubleArrayOf(8.0, 9.5, 7.0)
)

df
    .filter(col<Double>("score") >= 8.0)
    .select("name", "score")
```

## Exit criteria

A developer can perform a normal small tabular analysis without another data library.

---

# Phase 4 — GroupBy, Joins and Statistics

## Objective

Cover the operations required by real analytical work.

## GroupBy

- [ ] count
- [ ] sum
- [ ] mean
- [ ] min
- [ ] max
- [ ] variance
- [ ] multiple aggregations
- [ ] multiple grouping keys

## Joins

- [ ] inner
- [ ] left
- [ ] right where architecture supports it cleanly
- [ ] full outer later if necessary
- [ ] semi/anti later

## Statistics

- [ ] describe
- [ ] variance
- [ ] standard deviation
- [ ] quantiles
- [ ] covariance
- [ ] correlation

## Exit criteria

KodivX can handle common exploratory analytics and relational table operations.

---

# Phase 5 — Expression System

## Objective

Separate **what users request** from **how it executes**.

## Deliverables

- [ ] expression AST
- [ ] typed literals
- [ ] column references
- [ ] arithmetic expressions
- [ ] boolean expressions
- [ ] casts
- [ ] null expressions
- [ ] aggregate expressions
- [ ] aliases
- [ ] expression type inference
- [ ] expression validation
- [ ] expression pretty printer

Example:

```kotlin
val expr =
    (col<Double>("price") * col<Int>("quantity")) > 1_000
```

## Exit criteria

Core analytical operations can be represented without arbitrary row-by-row user lambdas.

---

# Phase 6 — Lazy Query Engine

## Objective

Build the foundation for Polars/DuckDB-style execution while keeping the API Kotlin-simple.

## Logical plan

- [ ] scan
- [ ] filter
- [ ] project
- [ ] aggregate
- [ ] sort
- [ ] join
- [ ] limit

## Lazy API

```kotlin
val q = df.lazy()
    .filter(col<Int>("age") >= 18)
    .select("name", "age")

val result = q.collect()
```

## Diagnostics

- [ ] `explain()`
- [ ] logical plan output
- [ ] optimized plan output

## Exit criteria

An eager DataFrame pipeline can be expressed through a lazy plan and produce equivalent results.

---

# Phase 7 — Optimizer and Vectorized Execution

## Objective

Make the engine intelligently avoid unnecessary work.

## Optimizer v1

- [ ] constant folding
- [ ] simplify boolean expressions
- [ ] combine filters
- [ ] remove unused projections
- [ ] projection pushdown
- [ ] predicate pushdown
- [ ] limit pushdown

## Physical execution

- [ ] batch abstraction
- [ ] vector kernels
- [ ] filter kernel
- [ ] project kernel
- [ ] aggregate kernel
- [ ] hash group-by
- [ ] hash join
- [ ] sorting

## Exit criteria

Benchmarks demonstrate that optimization reduces work on representative workloads, with correctness tests proving optimized and unoptimized plans are equivalent.

---

# Phase 8 — CSV and JSON I/O

## Objective

Make KodivX practical without requiring external conversion tools.

## CSV

- [ ] streaming parser
- [ ] configurable delimiter
- [ ] headers
- [ ] quotes/escaping
- [ ] null values
- [ ] schema inference
- [ ] explicit schemas
- [ ] projection where feasible
- [ ] CSV writer

## JSON

- [ ] JSON array-of-objects
- [ ] NDJSON
- [ ] explicit schema
- [ ] basic inference
- [ ] writer

## Exit criteria

Common datasets can be loaded and written on every primary target where filesystem/browser APIs permit it.

---

# Phase 9 — Cross-Platform Hardening

## Objective

Prove that "Multiplatform" is a product property, not a compile checkbox.

## Sample applications

- [ ] JVM CLI
- [ ] Android
- [ ] iOS
- [ ] desktop
- [ ] Kotlin/JS browser
- [ ] Kotlin/Wasm browser when sufficiently supported

Each sample should run the same core transformation:

```text
load → filter → group → aggregate → display
```

## Test matrix

For every supported target, verify:

- data type semantics;
- null behavior;
- floating-point edge cases;
- sorting;
- joins;
- Unicode strings;
- CSV parsing;
- expression behavior;
- lazy/eager equivalence.

## Exit criteria

The same documented KodivX example produces semantically equivalent results across supported targets.

---

# Phase 10 — Parquet

## Objective

Add the most important analytical file format.

## Features

- [ ] read schema
- [ ] primitive columns
- [ ] strings
- [ ] nulls
- [ ] timestamps
- [ ] projection
- [ ] row groups
- [ ] lazy scan
- [ ] metadata
- [ ] writer
- [ ] predicate pruning where supported

## Architecture requirement

Parquet must plug into the existing scan/plan system:

```text
scanParquet()
     ↓
Logical Scan
     ↓
optimizer
     ↓
physical Parquet scan
```

not bypass it with a separate API architecture.

## Exit criteria

Large Parquet files can be queried without requiring full-table materialization when projection/filter information is available.

---

# Phase 11 — Apache Arrow Interoperability

## Objective

Make KodivX a good citizen of the modern data ecosystem.

## Features

- [ ] Arrow type mapping
- [ ] validity bitmap mapping
- [ ] primitive array import/export
- [ ] string array import/export
- [ ] record batch/frame conversion
- [ ] JVM Arrow adapter
- [ ] native/C Data Interface investigation
- [ ] copy/zero-copy documentation

## Rule

Never advertise "zero-copy" unless the specific conversion genuinely avoids copying.

## Exit criteria

A supported Arrow batch can enter KodivX, undergo computation, and return to Arrow with type/null semantics preserved.

---

# Phase 12 — SQL Layer

## Objective

Allow users to query KodivX data through SQL without creating a second engine.

## Components

```text
SQL
 ↓
Parser
 ↓
SQL AST
 ↓
Binder / Resolver
 ↓
KodivX Logical Plan
 ↓
Existing optimizer
 ↓
Existing execution engine
```

## MVP

- [ ] SELECT
- [ ] aliases
- [ ] expressions
- [ ] WHERE
- [ ] GROUP BY
- [ ] aggregates
- [ ] ORDER BY
- [ ] LIMIT
- [ ] INNER JOIN
- [ ] LEFT JOIN

Example:

```kotlin
val result = sql(
    """
    SELECT country, AVG(price) AS avg_price
    FROM sales
    WHERE price > 100
    GROUP BY country
    """,
    tables = mapOf("sales" to sales)
)
```

## Exit criteria

SQL and Kotlin DSL plans converge into the same logical plan/execution pipeline.

---

# Phase 13 — JVM/Server Integrations

## Objective

Make KodivX highly useful in production Kotlin backends.

## Integrations

- [ ] JDBC import
- [ ] JDBC export
- [ ] DuckDB adapter
- [ ] Arrow Java adapter stabilization
- [ ] Kotlin Notebook integration
- [ ] Ktor examples
- [ ] Spring examples if demand exists

Example:

```kotlin
val frame = jdbc.read(
    connection,
    "SELECT * FROM orders"
)
```

## Exit criteria

KodivX fits naturally into real JVM data services without contaminating multiplatform core modules with JVM dependencies.

---

# Phase 14 — Performance Acceleration

## Objective

Accelerate hotspots only after the portable engine is correct and benchmarked.

## JVM opportunities

- specialized loops;
- JIT-friendly kernels;
- Java Vector API where practical;
- parallel batch execution.

## Native opportunities

- SIMD-friendly loops;
- platform BLAS;
- Apple Accelerate;
- C/C++ numerical libraries behind optional adapters.

## Web opportunities

- typed arrays;
- Wasm linear memory;
- Wasm SIMD when stable/useful;
- worker-based parallelism where platform support makes it worthwhile.

## Parallel execution

Potential model:

```text
Partition 1 ──→ Worker
Partition 2 ──→ Worker
Partition 3 ──→ Worker
Partition 4 ──→ Worker
                      ↓
                    Merge
```

Avoid parallelization for small operations where overhead is greater than the work.

## Exit criteria

Every acceleration backend has:

- correctness parity;
- fallback behavior;
- reproducible benchmark evidence;
- documented supported targets.

---

# Phase 15 — Typed DataFrame Experience

## Objective

Use Kotlin's type system as a competitive advantage.

Possible direction:

```kotlin
data class Sale(
    val country: String,
    val price: Double,
    val quantity: Int
)
```

Then:

```kotlin
val sales = frameOf<Sale>(...)

sales.filter {
    price > 100.0
}
```

Potential technologies:

- generated schemas;
- compiler plugin;
- KSP where appropriate;
- reified generic APIs.

This phase needs careful design because compiler tooling increases maintenance cost.

## Exit criteria

Typed access significantly improves safety/IDE experience without creating an incompatible parallel API.

---

# Phase 16 — Stable 1.0

## Objective

Make KodivX dependable enough that applications can adopt it without expecting constant source-breaking changes.

## 1.0 requirements

### API

- [ ] public API review
- [ ] naming consistency
- [ ] deprecation policy
- [ ] API compatibility checks
- [ ] semantic versioning rules

### Reliability

- [ ] fuzz/property testing
- [ ] integration tests
- [ ] platform test matrix
- [ ] memory/leak tests where applicable
- [ ] large-data stress tests

### Documentation

- [ ] complete API reference
- [ ] getting started
- [ ] arrays guide
- [ ] DataFrame guide
- [ ] lazy queries guide
- [ ] I/O guide
- [ ] performance guide
- [ ] mobile guide
- [ ] web guide
- [ ] migration notes

### Releases

- [ ] Maven Central publication
- [ ] signed/reproducible release process
- [ ] changelog
- [ ] release notes
- [ ] BOM/version alignment if useful

---

# 24. Post-1.0 Roadmap

These are directions, not promises.

## 2.x candidates

- richer NDArray;
- more statistical functions;
- window expressions;
- pivot/unpivot;
- categorical columns;
- nested columns;
- better Parquet pushdown;
- streaming queries;
- SQL expansion;
- stronger Arrow C Data Interface;
- native acceleration;
- improved typed schemas.

## 3.x / research candidates

- sparse data;
- external-memory execution;
- spill-to-disk;
- memory-mapped arrays;
- query compilation;
- GPU backend experiments;
- dataframe-to-tensor zero/low-copy adapters;
- distributed execution research;
- remote data sources;
- object stores;
- DataFusion/Substrait-style plan interoperability investigation.

---

# 25. AI / ML Integration Direction

KodivX should not become an ML framework initially.

Instead, become an excellent **data layer for ML**.

```text
CSV / JSON / Parquet
        ↓
      KodivX
        ↓
 cleaning / filtering
        ↓
 feature engineering
        ↓
 Array / Matrix / Arrow
        ↓
 ML runtime / model
```

Potential later adapters:

- ONNX Runtime;
- TensorFlow Lite where relevant;
- KotlinDL if ecosystem demand exists;
- JVM ML libraries;
- native inference systems;
- Python bridge for research environments.

The core priority is making data transfer efficient and predictable.

---

# 26. Performance Benchmark Plan

Performance work requires a permanent benchmark suite.

## Benchmark categories

### Arrays

- addition
- multiplication
- sum
- mean
- dot
- reshape/view
- slicing

### DataFrames

- column select
- filter
- arithmetic expression
- sort
- group-by
- aggregation
- join

### I/O

- CSV parse
- CSV write
- Parquet read
- Parquet projected read
- Arrow conversion

### Query engine

- eager vs lazy
- projection pushdown
- predicate pushdown
- chained filters
- group/aggregate
- join + aggregate

## Dataset sizes

Use multiple scales:

```text
10K rows
100K rows
1M rows
10M rows
```

and larger only where the machine/benchmark purpose supports it.

## Metrics

Record:

- wall-clock time;
- throughput;
- peak memory;
- allocations where measurable;
- startup/warm-up behavior;
- artifact size for mobile/web;
- load/parse time.

## Competitor baselines

Depending on operation:

- Kotlin standard library;
- Kotlin DataFrame;
- Multik;
- DuckDB;
- Java/JVM analytical libraries;
- pandas;
- Polars;
- NumPy.

Cross-language benchmarks must document:

- machine;
- OS;
- runtime;
- versions;
- warm-up;
- data type;
- dataset;
- operation semantics;
- thread count.

Never benchmark different semantics and call the result a fair comparison.

---

# 27. Performance Acceptance Philosophy

Avoid goals such as:

> "KodivX must be 10x faster than pandas."

Instead use engineering goals:

1. No accidental per-cell boxing for primitive analytical paths.
2. No unnecessary full-table copies.
3. Operations should scale near the expected algorithmic complexity.
4. Lazy optimization must measurably reduce unnecessary I/O/computation.
5. Optimized backends must never silently change results.
6. Regressions above an agreed threshold should fail benchmark review.
7. Performance claims must link to reproducible code.

---

# 28. Testing Strategy

## Unit tests

Every data type and operation.

## Property-based tests

Examples:

```text
sort(sort(x)) == sort(x)
filter(true) == original
select(allColumns) == original
lazy.collect() == eagerResult
sum(partitions) == sum(all) within numeric rules
```

## Differential tests

For selected operations, compare against trusted engines.

Example:

```text
KodivX groupBy result
vs
DuckDB equivalent query
```

or:

```text
KodivX array operation
vs
reference implementation
```

## Fuzzing

Prioritize:

- CSV parser;
- JSON parser;
- expression parser;
- SQL parser;
- Parquet metadata;
- slicing/index calculations.

## Golden tests

For:

- table rendering;
- explain plans;
- error messages;
- schema output.

---

# 29. Error Design

Errors should explain:

1. what went wrong;
2. where it happened;
3. expected type/schema;
4. actual type/schema;
5. likely fix where possible.

Bad:

```text
IllegalArgumentException
```

Better:

```text
KodivX type error:
Expression: price + country
Expected numeric column for right operand.
Found: Utf8 column "country".
```

---

# 30. Documentation Strategy

Documentation should teach progressively.

## Level 1 — Five-minute start

```kotlin
val df = readCsv("sales.csv")
println(df.head())
```

## Level 2 — Everyday operations

- select;
- filter;
- sort;
- group;
- join;
- missing values.

## Level 3 — Analytical engine

- expressions;
- lazy queries;
- explain;
- Parquet;
- SQL.

## Level 4 — Systems

- memory model;
- custom kernels;
- Arrow;
- execution backends;
- optimization.

## Level 5 — Contributors

- architecture;
- module boundaries;
- benchmark rules;
- release process.

---

# 31. Example README Experience

A potential future README opening:

```markdown
# KodivX

Fast, simple data computing for Kotlin — everywhere.

```kotlin
val sales = readCsv("sales.csv")

val result = sales
    .filter(col<Double>("price") > 100)
    .groupBy("country")
    .agg(mean("price"))

println(result)
```

Run the same data logic across JVM, Android, iOS, desktop and web.
```

The first screen of the README should show **what KodivX feels like**, not a long architectural explanation.

---

# 32. API Naming Rules

Use predictable verbs.

Preferred:

```text
readCsv
scanParquet
select
filter
withColumn
rename
drop
sortBy
groupBy
agg
join
collect
explain
writeCsv
writeParquet
```

Avoid unnecessary synonyms such as simultaneously exposing:

```text
filter
where
subset
retain
queryRows
```

unless there is a strong API reason.

A small vocabulary makes a library feel simpler.

---

# 33. Compatibility Policy

Before 1.0:

- changes allowed;
- use release notes;
- deprecate where practical;
- avoid gratuitous churn.

After 1.0:

- semantic versioning;
- deprecate before removal;
- machine-check binary/source API where tools support it;
- publish migration guides for major releases.

Data-format compatibility should be treated separately from Kotlin API compatibility.

---

# 34. Security and Robustness

Data libraries parse untrusted input.

KodivX should protect against:

- malformed CSV/JSON;
- maliciously huge declared sizes;
- integer overflow in allocation calculations;
- corrupted Parquet metadata;
- pathological nesting;
- zip/decompression bombs when compressed formats are added;
- unbounded memory growth;
- unsafe native memory access;
- SQL injection in integration APIs.

SQL APIs should support bound parameters where applicable rather than encouraging string concatenation for user-provided values.

---

# 35. Mobile-Specific Requirements

Mobile is not a marketing checkbox.

## Android/iOS goals

- small base artifact;
- no mandatory SQL engine;
- no mandatory Arrow runtime;
- no mandatory BLAS runtime;
- predictable memory;
- streaming file readers;
- cancelable long operations where practical;
- API usable from normal application code;
- no server assumptions.

Example use cases:

- offline analytics;
- sensor analysis;
- local financial summaries;
- local CSV/JSON inspection;
- preprocessing before on-device inference;
- dashboards.

---

# 36. Web-Specific Requirements

Browser execution creates different constraints.

Priorities:

- no filesystem assumptions;
- support `File`, `Blob`, byte arrays or platform adapters;
- streaming where browser APIs permit;
- avoid blocking UI threads for heavy workloads;
- support workers in later phases;
- keep bundle sizes visible in CI;
- benchmark both JS and Wasm;
- never assume Wasm will automatically be faster than JS.

Example:

```kotlin
val df = readCsv(uploadedFile)
val summary = df.groupBy("category").agg(sum("amount"))
```

All processing may remain client-side for compatible workloads.

---

# 37. Desktop / Server Requirements

## Desktop

- local files;
- large datasets;
- native acceleration;
- Kotlin Notebook;
- interactive display integration.

## Server

- concurrency;
- predictable memory;
- database integration;
- Parquet/Arrow;
- streaming;
- observability;
- minimal shared mutable global state.

---

# 38. Concurrency Model

Do not expose a confusing threading API in v1.

Default:

```kotlin
df.groupBy(...).agg(...)
```

The engine decides whether a supported backend can parallelize.

Advanced configuration can later expose:

```kotlin
KodivX.configure {
    parallelism = 8
}
```

Principles:

- deterministic output semantics;
- no hidden uncontrolled thread explosion;
- respect mobile/web resource constraints;
- allow single-thread mode;
- benchmark parallel thresholds.

---

# 39. Streaming and Out-of-Core Direction

Not required for the earliest MVP, but the execution architecture should not prevent it.

Future:

```kotlin
scanCsv("huge.csv")
    .filter(...)
    .select(...)
    .collect()
```

should process batches without holding the complete source in memory where possible.

Longer term:

- external sort;
- spillable group-by;
- spillable joins;
- memory budget;
- temporary storage manager.

Do not build these before ordinary in-memory execution is excellent.

---

# 40. Release Milestones

A reasonable milestone naming scheme:

## `0.1` — Core

- buffers;
- arrays;
- basic columns.

## `0.2` — Frames

- DataFrame;
- filter/select/sort;
- basic stats.

## `0.3` — Relational

- group-by;
- joins;
- richer aggregation.

## `0.4` — Expressions

- typed expression engine.

## `0.5` — Lazy

- logical plans;
- optimizer foundation;
- collect/explain.

## `0.6` — I/O

- CSV/JSON;
- streaming basics.

## `0.7` — Multiplatform hardening

- sample apps;
- platform consistency.

## `0.8` — Analytical formats

- Parquet;
- Arrow interoperability.

## `0.9` — Query ecosystem

- SQL MVP;
- JVM/DuckDB/JDBC adapters;
- performance stabilization.

## `1.0` — Stable

- API stabilization;
- production documentation;
- robust compatibility policy;
- benchmark report;
- release automation.

Versions are milestones, not deadlines.

---

# 41. What NOT to Build First

This section is important.

Do not begin with:

```text
GPU engine
distributed cluster
complete SQL parser
full Arrow implementation from scratch
complete Parquet implementation from scratch
compiler plugin
100 statistical functions
Spark competitor
Python bridge
AI model training
cloud warehouse connectors
```

First prove this:

```kotlin
val df = readCsv("data.csv")

df
    .filter(col<Int>("age") > 18)
    .groupBy("country")
    .agg(mean("income"))
```

works beautifully, correctly and quickly across the core supported platforms.

---

# 42. First 20 Engineering Tasks

A concrete starting backlog:

1. [ ] Create `kodivx` repository.
2. [ ] Configure KMP targets.
3. [ ] Configure CI.
4. [ ] Add code formatting/linting.
5. [ ] Create `kodivx-core`.
6. [ ] Define `DataType`.
7. [ ] Define `Field`.
8. [ ] Define `Schema`.
9. [ ] Implement validity bitmap.
10. [ ] Implement `IntBuffer`.
11. [ ] Implement `LongBuffer`.
12. [ ] Implement `DoubleBuffer`.
13. [ ] Implement UTF-8 string storage prototype.
14. [ ] Implement `Column<T>` abstraction.
15. [ ] Implement `IntColumn`.
16. [ ] Implement `DoubleColumn`.
17. [ ] Implement `StringColumn`.
18. [ ] Implement minimal `DataFrame`.
19. [ ] Add `select()` and `filter()`.
20. [ ] Create benchmark comparing column scan against a naive boxed representation.

Do not start with SQL.

---

# 43. First Public Demo

A powerful early demo should run on multiple platforms with the same shared source.

Dataset:

```text
sales.csv
```

Code:

```kotlin
val sales = readCsv("sales.csv")

val summary = sales
    .filter(col<Double>("amount") > 100.0)
    .groupBy("country")
    .agg(
        count().alias("orders"),
        sum("amount").alias("revenue"),
        mean("amount").alias("average")
    )
    .sortBy(desc("revenue"))

println(summary)
```

Run it in:

- JVM CLI;
- Android;
- iOS;
- browser.

That demonstration communicates the project better than dozens of feature claims.

---

# 44. Success Metrics

Do not judge KodivX only by GitHub stars.

## Technical metrics

- supported platform CI pass rate;
- benchmark regressions;
- memory allocations;
- artifact size;
- startup performance;
- test coverage of core invariants;
- API compatibility;
- crash/error rate in integration tests.

## Ecosystem metrics

- external contributors;
- applications using KodivX;
- documentation visits;
- issue resolution;
- independent benchmarks;
- integrations created by others.

## Developer experience metrics

- time to first DataFrame;
- number of concepts required for basic analysis;
- quality of errors;
- amount of code required for common tasks.

---

# 45. Risks

## Risk 1 — Scope explosion

Trying to build NumPy + pandas + Polars + DuckDB + Arrow simultaneously will likely stall the project.

**Mitigation:** phased architecture and strict non-goals.

## Risk 2 — Lowest-common-denominator multiplatform design

Trying to make every feature identical everywhere can lead to weak performance.

**Mitigation:** common semantics + platform-specific acceleration.

## Risk 3 — Too many abstraction layers

A theoretically elegant engine can become painful to use.

**Mitigation:** beginner API first; internals remain internal.

## Risk 4 — Premature native optimization

Native code complicates builds and distribution.

**Mitigation:** portable implementation first, optimized optional backends second.

## Risk 5 — JVM bias

A Kotlin project can accidentally become JVM-only through dependencies.

**Mitigation:** dependency review and commonMain ownership rules.

## Risk 6 — Python imitation

Copying pandas syntax mechanically may produce unnatural Kotlin.

**Mitigation:** copy the simplicity, not the language semantics.

## Risk 7 — Benchmark marketing

Cherry-picked benchmarks damage trust.

**Mitigation:** reproducible benchmark repository and clear methodology.

## Risk 8 — Maintaining parsers/formats

CSV, Parquet, Arrow and SQL are large technical domains.

**Mitigation:** integrate mature standards/libraries where appropriate instead of rewriting everything.

---

# 46. Competitive Advantage

KodivX should not try to win because it has more functions.

Its potential differentiation is the combination:

```text
Simple API
   +
Kotlin type safety
   +
columnar analytical engine
   +
Kotlin Multiplatform
   +
mobile / desktop / web / server
   +
interoperable formats
```

Any one of those alone is not enough.

The product identity comes from combining them coherently.

---

# 47. Project Philosophy

A useful internal motto:

> **Portable first. Fast by design. Native when it matters. Simple always.**

Decision hierarchy:

```text
1. Correct?
2. Simple for users?
3. Consistent across platforms?
4. Efficient architecture?
5. Measured performance?
6. Worth the complexity?
```

---

# 48. Recommended v1 Feature Boundary

A realistic stable core should focus on this:

```text
KodivX 1.0
│
├── Arrays
│   ├── vectors
│   ├── matrices
│   ├── arithmetic
│   └── basic statistics
│
├── DataFrames
│   ├── typed columns
│   ├── select/filter/sort
│   ├── group/aggregate
│   └── joins
│
├── Expressions
│   ├── arithmetic
│   ├── boolean
│   └── aggregate
│
├── Lazy
│   ├── plans
│   ├── rule optimizer
│   └── explain
│
├── I/O
│   ├── CSV
│   ├── JSON/NDJSON
│   └── Parquet
│
├── Interop
│   └── Arrow
│
├── SQL
│   └── useful analytical subset
│
└── Platforms
    ├── JVM
    ├── Android
    ├── iOS/native
    ├── desktop
    ├── JS
    └── Wasm where production maturity permits
```

Everything else can evolve after this foundation.

---

# 49. Ecosystem Research Notes — September 2026

These facts influenced this roadmap:

- Kotlin Multiplatform currently supports Android, iOS, desktop/JVM and server/JVM as stable core targets; Kotlin/JS web is stable and Kotlin/Wasm web is Beta.
- JetBrains' Kotlin roadmap continues to position Kotlin Multiplatform as a major strategic direction.
- Kotlin DataFrame is currently JVM-only, although multiplatform support is being explored.
- Apache Arrow defines a language-independent columnar memory format optimized for analytical access, vectorization and efficient interchange.
- DuckDB provides a JVM/JDBC client with Apache Arrow interchange capabilities.

These are reasons to keep KodivX's **portable logical/data core independent from heavy JVM-only integrations**.

---

# 50. Recommended Technical Research Before Coding Each Major Layer

## Before buffers

Study:

- Apache Arrow columnar memory specification;
- primitive array memory models across KMP targets;
- Kotlin/Native memory behavior;
- JS typed arrays;
- Wasm linear memory.

## Before DataFrame

Study:

- pandas semantics;
- Polars expression API;
- Kotlin DataFrame API;
- Arrow schemas.

## Before optimizer

Study:

- relational algebra;
- predicate/projection pushdown;
- Volcano-style planning concepts;
- DuckDB and DataFusion architecture papers/docs.

## Before Parquet

Study:

- Parquet physical/logical types;
- row groups;
- pages;
- statistics;
- dictionary encoding;
- compression;
- predicate pruning.

## Before SQL

Study:

- parser architecture;
- name binding;
- type coercion;
- relational algebra lowering;
- SQL null/three-valued logic.

---

# 51. Decision Records to Write Early

Create ADRs for important choices.

Suggested first ADRs:

```text
ADR-001: Columnar storage as primary table representation
ADR-002: Immutable public DataFrame operations
ADR-003: Validity bitmap null representation
ADR-004: commonMain owns logical engine
ADR-005: Optional platform acceleration
ADR-006: Expression AST for optimized operations
ADR-007: SQL lowers into the same logical plan
ADR-008: Arrow as interoperability layer, not mandatory core runtime
ADR-009: Modular artifacts
ADR-010: Benchmark-before-optimization policy
```

This prevents architecture from drifting based on whichever feature is being implemented that week.

---

# 52. Definition of Done for a Feature

A KodivX feature is not done merely when the code compiles.

A normal feature should have:

- [ ] implementation;
- [ ] tests;
- [ ] null behavior tests;
- [ ] platform tests;
- [ ] docs;
- [ ] example;
- [ ] error behavior;
- [ ] benchmark if performance-sensitive;
- [ ] changelog entry when externally visible;
- [ ] no accidental dependency added to common core.

---

# 53. Contributor-Friendly Architecture

New contributors should be able to work at different levels.

```text
Beginner
   ↓
docs / examples / tests / simple expressions

Intermediate
   ↓
DataFrame operations / I/O / statistics

Advanced
   ↓
optimizer / execution kernels / memory / Parquet

Systems expert
   ↓
SIMD / native / Arrow / query compilation / Wasm
```

This makes the project easier to grow into a community.

---

# 54. Suggested GitHub Labels

```text
area:array
area:dataframe
area:expr
area:query
area:optimizer
area:io
area:parquet
area:arrow
area:sql
area:jvm
area:android
area:ios
area:js
area:wasm
area:native
performance
correctness
documentation
good-first-issue
help-wanted
breaking-change
benchmark
design
```

---

# 55. Long-Term North Star

The long-term ideal is that a Kotlin developer can write:

```kotlin
val sales = scanParquet("sales/*.parquet")

val result = sales
    .filter(col<Double>("price") > 100)
    .withColumn(
        "revenue",
        col<Double>("price") * col<Int>("quantity")
    )
    .groupBy("country")
    .agg(
        sum("revenue"),
        mean("price")
    )
    .sortBy(desc("revenue"))
    .collect()
```

and KodivX can choose an efficient execution strategy for the current platform.

On a phone:

```text
portable/mobile-optimized engine
```

On JVM server:

```text
parallel/JIT/vectorized engine
```

On native desktop:

```text
native/SIMD acceleration
```

In a browser:

```text
JS or Wasm engine
```

The source-level mental model remains the same.

---

# 56. Final Project Goal

KodivX should ultimately answer this question:

> **Why should data computing require a Python runtime or a server when Kotlin applications already run everywhere?**

The answer KodivX should provide is:

> It shouldn't. Kotlin developers should be able to perform serious numerical and analytical data work directly inside their applications using one simple, fast and portable API.

That is the project.

---

# 57. Immediate Recommended Starting Point

Do not begin by writing hundreds of DataFrame functions.

Begin with this dependency chain:

```text
DataType
   ↓
Buffer
   ↓
Validity Bitmap
   ↓
Column
   ↓
Schema
   ↓
DataFrame
   ↓
Expression
   ↓
Filter / Select
   ↓
Benchmark
```

Then build outward.

The first technical milestone should be:

> **A columnar DataFrame containing primitive and string columns that can filter and select data correctly on JVM, Android, iOS and web from shared Kotlin code.**

Once that foundation is excellent, the rest of KodivX becomes much easier to build.

---

# 58. Official References

These are useful starting references for design research:

- Kotlin Multiplatform: https://kotlinlang.org/multiplatform/
- Kotlin Multiplatform supported platforms: https://kotlinlang.org/docs/multiplatform/supported-platforms.html
- Kotlin roadmap: https://kotlinlang.org/docs/roadmap.html
- Kotlin DataFrame: https://kotlin.github.io/dataframe/
- Kotlin DataFrame FAQ: https://kotlin.github.io/dataframe/faq.html
- Multik: https://kotlin.github.io/multik/
- Apache Arrow: https://arrow.apache.org/
- Arrow Columnar Format: https://arrow.apache.org/docs/format/Columnar.html
- DuckDB Java/JDBC: https://duckdb.org/docs/current/clients/java/overview
- Polars documentation: https://docs.pola.rs/
- NumPy documentation: https://numpy.org/doc/
- pandas documentation: https://pandas.pydata.org/docs/

---

## One-line mission

> **KodivX makes fast analytical data computing simple and portable across the Kotlin ecosystem.**

## One-line engineering rule

> **Keep the public API small, the data columnar, the core portable, and every performance claim measurable.**

