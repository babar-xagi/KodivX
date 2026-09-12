# Architecture Decision Records (ADRs)

This directory documents key architectural and design decisions made for the KodivX project.

| ADR | Title | Status |
|---|---|---|
| [ADR-0001](0001-columnar-storage-primary-representation.md) | Columnar storage as primary table representation | Accepted |
| [ADR-0002](0002-immutable-public-dataframe-operations.md) | Immutable public DataFrame operations | Accepted |
| [ADR-0003](0003-validity-bitmap-null-model.md) | Validity bitmap null representation | Accepted |
| [ADR-0004](0004-commonmain-owns-logical-engine.md) | commonMain owns the logical engine | Accepted |
| [ADR-0005](0005-optional-platform-acceleration.md) | Optional platform acceleration backends | Accepted |
| [ADR-0006](0006-expression-ast-for-optimized-operations.md) | Expression AST for optimized operations | Accepted |
| [ADR-0007](0007-sql-lowering-into-same-logical-plan.md) | SQL lowers into the same logical plan | Accepted |
| [ADR-0008](0008-arrow-as-interop-not-mandatory-core.md) | Arrow as interoperability layer, not mandatory core runtime | Accepted |
| [ADR-0009](0009-modular-artifacts.md) | Modular artifact boundaries | Accepted |
| [ADR-0010](0010-benchmark-before-optimization.md) | Benchmark-before-optimization policy | Accepted |
| [ADR-0011](0011-selection-vectors-for-filtering.md) | Selection vectors for intermediate relational operations | Accepted |
| [ADR-0012](0012-utf8-binary-string-layout.md) | Continuous Arrow-compatible LargeUtf8 layout for strings | Accepted |
| [ADR-0013](0013-kotlinx-io-multiplatform-streams.md) | Adopt kotlinx-io as multiplatform stream contract | Accepted |
| [ADR-0014](0014-specialized-vector-kernels.md) | Specialized vector kernels with single-batch dynamic dispatch | Accepted |
