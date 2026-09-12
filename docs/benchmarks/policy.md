# KodivX Benchmark & Performance Policy

> **Policy Motto:** *No performance claim without reproducible code, documented hardware, and verified semantic equivalence.*

---

## 1. Principles of Benchmarking

1. **Correctness Before Speed:** A faster algorithm that produces incorrect floating-point, null, or Unicode results is rejected.
2. **Semantic Equivalence:** Comparative benchmarks against Python (Pandas, Polars, NumPy) or DuckDB must execute the exact same logical operation on the exact same dataset.
3. **No Hidden Warmup / Micro-cherry-picking:** JVM benchmarks must use JMH with proper warmup iterations. Native benchmarks must measure steady-state execution and memory peaks.
4. **Transparent Environment Specification:** All reports must record OS, CPU model, RAM, JDK version, Kotlin compiler version, thread count, and dataset generation seed.

---

## 2. Standard Benchmark Datasets

Benchmarking harnesses in `benchmarks/` evaluate performance across 4 standard data scales:
- **10K rows:** Cache-resident testing & overhead measurement.
- **100K rows:** Real-world mobile and client-side web application scale.
- **1M rows:** Standard analytical batch scale.
- **10M rows:** Out-of-L3 cache / multi-core memory-bandwidth scale.

---

## 3. Metrics Recorded

- **Throughput:** Operations per second / Rows processed per second.
- **Allocation Footprint:** Heap memory allocated per row / GC pause times.
- **Binary Footprint:** Artifact size impact for Android APKs and WebAssembly bundles.
- **Latency / Wall-Clock Duration:** Time to complete pipeline execution.
