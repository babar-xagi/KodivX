# ADR-0007: SQL Lowers into the Same Logical Plan

## Status
Accepted

## Context
Many libraries maintain two parallel query engines: one for programmatic DSL queries and another for SQL queries. This leads to duplicate maintenance, divergent bug reports, and uneven optimization.

## Decision
KodivX parses SQL directly into the standard `LogicalPlan` and `Expr` AST used by the Kotlin DataFrame DSL. The optimizer and physical execution engine remain 100% shared.

## Consequences
- Single query planner, optimizer, and execution engine to maintain.
- SQL queries immediately benefit from all optimizations added for Kotlin DSL queries (and vice versa).
- Minimal binary size impact when adding SQL support.
