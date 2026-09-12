# ADR-0014: Specialized Vector Kernels with Single-Batch Dynamic Dispatch

## Status
Accepted

## Context
Exposing row access through generic polymorphic interfaces (`interface Column<T> { operator fun get(i: Int): T? }`) causes megamorphic virtual dispatch and boxing on every single element in tight execution loops, severely degrading throughput.

## Decision
In internal execution paths, vector kernels dispatch dynamically once per batch/column type (e.g. `DoubleAddKernel`), and then execute unrolled, monomorphic loops directly over contiguous primitive arrays.

## Consequences
- 100% loop unrolling and JIT/LLVM autovectorization friendliness.
- Zero per-element virtual method calls or boxing overhead.
- Performance in primitive mathematical operations rivals native C/NumPy loops.
