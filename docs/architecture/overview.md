# KodivX Architecture Overview

> **Design Tagline:** *Simple data. Native speed. Everywhere.*

KodivX is a Kotlin Multiplatform analytical computing library. Its design balances **ergonomics** for end users with **hardware-conscious performance** internally.

---

## 1. System Layers

```text
┌─────────────────────────────────────────────────────────────┐
│                       PUBLIC API                            │
│ array(...) / dataFrameOf(...) / readCsv(...) / col(...)     │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    LOGICAL DATA MODEL                       │
│ DataType / Field / Schema / Column / DataFrame / Scalar     │
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
│                    PHYSICAL OPERATORS                       │
│ Batched RecordChunks / SelectionVectors / Vectorized Kernels│
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                    MEMORY & BUFFER SYSTEM                   │
│ Contiguous Primitive Arrays / 64-bit SWAR Bitmaps / UTF-8   │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Core Architectural Tenets

### 2.1 Columnar Storage
DataFrames are stored column-by-column rather than row-by-row. Analytical queries typically touch only a small subset of columns; storing columns contiguously ensures CPU caches are saturated with relevant data without reading unused attributes.

### 2.2 Unboxed Primitive Columns
Columns containing primitive types (`Int`, `Long`, `Float`, `Double`, `Boolean`) store values in flat primitive arrays or direct memory buffers. Primitive scalar iteration does not allocate heap wrapper objects.

### 2.3 Validity Bitmaps for Missing Data
Missing/null values are tracked using a compact bitmask where 1 bit represents the nullity of one row. Inner execution loops process 64 elements at a time using 64-bit word operations (`Long`), bypassing individual null checks when all 64 elements are valid.

### 2.4 Zero-Copy Filter Chaining with Selection Vectors
Filtering a chunk writes row indices to a reusable `SelectionVector` rather than allocating new arrays and copying memory. Downstream transformations evaluate over selected rows in-place.

### 2.5 Unified Expression and Plan Representation
The Kotlin DataFrame DSL, the Lazy Query API, and the Embedded SQL parser compile into the exact same `LogicalPlan` and `Expr` AST. There is only one execution and optimization engine.
