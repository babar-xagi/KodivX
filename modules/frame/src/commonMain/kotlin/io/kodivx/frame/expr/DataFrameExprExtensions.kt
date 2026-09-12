package io.kodivx.frame

import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.frame.*

/**
 * Evaluates an [Expr] predicate across all rows and returns a filtered [DataFrame] using a zero-copy [SelectionVector].
 */
fun DataFrame.filter(predicate: Expr): DataFrame {
    val evaluated = predicate.eval(this)
    require(evaluated is BooleanColumn) {
        "Filter expression must evaluate to a BooleanColumn, but got: ${evaluated::class.simpleName}"
    }

    val matchingIndices = IntArray(rowCount)
    var matchCount = 0

    for (i in 0 until rowCount) {
        if (evaluated.isValid(i) && evaluated.getBoolean(i)) {
            matchingIndices[matchCount++] = i
        }
    }

    val selection = SelectionVector(matchingIndices, 0, matchCount)
    val filteredCols = columns.map { it.filterWithSelection(selection) }
    return DataFrame(schema, filteredCols)
}

/**
 * Projects a new [DataFrame] by evaluating each [Expr] in [exprs].
 */
fun DataFrame.select(vararg exprs: Expr): DataFrame = select(exprs.toList())

/**
 * Projects a new [DataFrame] by evaluating a list of [Expr].
 */
fun DataFrame.select(exprs: List<Expr>): DataFrame {
    require(exprs.isNotEmpty()) { "At least one expression must be selected" }
    val newCols = exprs.map { expr -> expr.eval(this) }
    return dataFrameOf(newCols)
}

/**
 * Adds or replaces a column by evaluating [expr]. The column name is taken from [Expr.outputName].
 */
fun DataFrame.withColumn(expr: Expr): DataFrame = withColumn(expr.outputName, expr)

/**
 * Adds or replaces a column named [name] by evaluating [expr].
 */
fun DataFrame.withColumn(name: String, expr: Expr): DataFrame {
    val newCol = expr.eval(this).withName(name)
    return withColumn(newCol)
}

/**
 * Evaluates aggregate expressions over a [GroupedDataFrame].
 */
fun GroupedDataFrame.agg(vararg exprs: Expr): DataFrame {
    val aggregations = exprs.map { toAggregation(it) }
    return agg(aggregations)
}

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
