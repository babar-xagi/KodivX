package io.kodivx.frame

import io.kodivx.core.schema.Schema

/**
 * Lazy, declarative representation of a DataFrame query pipeline.
 * Computations are deferred until [collect] is invoked.
 */
class LazyDataFrame(val plan: LogicalPlan) {
    val schema: Schema get() = plan.schema

    // --- Relational & Query Operations ---

    fun filter(predicate: Expr): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Filter(plan, predicate))

    fun select(vararg columns: String): LazyDataFrame =
        select(columns.map { col(it) })

    fun select(vararg exprs: Expr): LazyDataFrame =
        select(exprs.toList())

    fun select(exprs: List<Expr>): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Projection(plan, exprs))

    fun withColumn(name: String, expr: Expr): LazyDataFrame {
        val currentCols = schema.fieldNames.map { col(it) as Expr }.toMutableList()
        val existingIdx = schema.indexOf(name)
        val namedExpr = if (expr is Expr.Alias && expr.aliasName == name) expr else expr.alias(name)
        if (existingIdx != -1) {
            currentCols[existingIdx] = namedExpr
        } else {
            currentCols.add(namedExpr)
        }
        return select(currentCols)
    }

    fun withColumn(expr: Expr): LazyDataFrame = withColumn(expr.outputName, expr)

    fun groupBy(vararg keys: String): LazyGroupBy = LazyGroupBy(plan, keys.toList())
    fun groupBy(keys: List<String>): LazyGroupBy = LazyGroupBy(plan, keys)

    fun sortBy(vararg orders: SortOrder): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Sort(plan, orders.toList()))

    fun sortBy(column: String, ascending: Boolean = true): LazyDataFrame =
        sortBy(SortOrder(column, ascending))

    fun join(
        other: LazyDataFrame,
        on: String,
        how: JoinType = JoinType.Inner,
        suffix: String = "_right"
    ): LazyDataFrame = join(other, listOf(on), listOf(on), how, suffix)

    fun join(
        other: LazyDataFrame,
        leftOn: String,
        rightOn: String,
        how: JoinType = JoinType.Inner,
        suffix: String = "_right"
    ): LazyDataFrame = join(other, listOf(leftOn), listOf(rightOn), how, suffix)

    fun join(
        other: LazyDataFrame,
        leftOn: List<String>,
        rightOn: List<String>,
        how: JoinType = JoinType.Inner,
        suffix: String = "_right"
    ): LazyDataFrame = LazyDataFrame(LogicalPlan.Join(plan, other.plan, leftOn, rightOn, how, suffix))

    fun limit(n: Int): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Limit(plan, 0, n))

    fun head(n: Int = 5): LazyDataFrame = limit(n)

    fun distinct(): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Distinct(plan))

    // --- Execution & Inspection ---

    /**
     * Optimizes the logical plan using rule-based transformations and executes it into an eager [DataFrame].
     */
    fun collect(): DataFrame = plan.optimize().execute()

    /**
     * Executes the unoptimized logical plan directly.
     */
    fun collectUnoptimized(): DataFrame = plan.execute()

    /**
     * Returns a diagnostic string with the logical plan and the optimized plan.
     */
    fun explain(showOptimized: Boolean = true): String = buildString {
        append("=== Logical Plan ===\n")
        append(plan.toTreeString())
        if (showOptimized) {
            append("\n\n=== Optimized Plan ===\n")
            append(plan.optimize().toTreeString())
        }
    }

    override fun toString(): String = explain()
}

/**
 * Lazy grouping operator awaiting aggregation operations.
 */
class LazyGroupBy(val plan: LogicalPlan, val groupKeys: List<String>) {
    fun agg(vararg aggregations: Aggregation): LazyDataFrame =
        LazyDataFrame(LogicalPlan.Aggregate(plan, groupKeys, aggregations.toList()))

    fun agg(vararg exprs: Expr): LazyDataFrame {
        val aggregations = exprs.map { toAggregation(it) }
        return LazyDataFrame(LogicalPlan.Aggregate(plan, groupKeys, aggregations))
    }

    fun count(): LazyDataFrame = agg(io.kodivx.frame.count())
    fun sum(vararg columns: String): LazyDataFrame = agg(*columns.map { io.kodivx.frame.sum(it) }.toTypedArray())
    fun mean(vararg columns: String): LazyDataFrame = agg(*columns.map { io.kodivx.frame.mean(it) }.toTypedArray())
    fun min(vararg columns: String): LazyDataFrame = agg(*columns.map { io.kodivx.frame.min(it) }.toTypedArray())
    fun max(vararg columns: String): LazyDataFrame = agg(*columns.map { io.kodivx.frame.max(it) }.toTypedArray())
}

/**
 * Creates a [LazyDataFrame] backed by an in-memory [DataFrame] scan.
 */
fun DataFrame.lazy(): LazyDataFrame =
    LazyDataFrame(LogicalPlan.Scan(DataSource.MemorySource(this)))

private fun toAggregation(expr: Expr): Aggregation = when (expr) {
    is Aggregation -> expr
    is Expr.Alias -> {
        val inner = toAggregation(expr.expr)
        inner.alias(expr.aliasName)
    }
    is Expr.Aggregate -> {
        val colName = when (val inner = expr.expr) {
            null -> null
            is Expr.ColumnRef -> inner.name
            else -> inner.outputName
        }
        when (expr.kind) {
            Expr.AggKind.Count -> Aggregation.Count(colName)
            Expr.AggKind.Sum -> Aggregation.Sum(colName ?: error("Sum requires a column"))
            Expr.AggKind.Mean -> Aggregation.Mean(colName ?: error("Mean requires a column"))
            Expr.AggKind.Min -> Aggregation.Min(colName ?: error("Min requires a column"))
            Expr.AggKind.Max -> Aggregation.Max(colName ?: error("Max requires a column"))
            Expr.AggKind.Variance -> Aggregation.Variance(colName ?: error("Variance requires a column"))
            Expr.AggKind.StdDev -> Aggregation.StdDev(colName ?: error("StdDev requires a column"))
        }
    }
    else -> error("Expected Aggregate expression in GroupBy.agg, got: ${expr::class.simpleName} (${expr.outputName})")
}
