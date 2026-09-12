package io.kodivx.frame

import io.kodivx.buffer.selection.SelectionVector
import kotlin.math.max
import kotlin.math.min

/**
 * Executes a [LogicalPlan] and returns an eager [DataFrame].
 */
fun LogicalPlan.execute(): DataFrame = when (this) {
    is LogicalPlan.Scan -> when (source) {
        is DataSource.MemorySource -> source.df
    }

    is LogicalPlan.Filter -> {
        val inDf = input.execute()
        inDf.filter(predicate)
    }

    is LogicalPlan.Projection -> {
        val inDf = input.execute()
        inDf.select(expressions)
    }

    is LogicalPlan.Aggregate -> {
        val inDf = input.execute()
        inDf.groupBy(groupKeys).agg(aggregations)
    }

    is LogicalPlan.Sort -> {
        val inDf = input.execute()
        inDf.sortBy(*orders.toTypedArray())
    }

    is LogicalPlan.Join -> {
        val leftDf = left.execute()
        val rightDf = right.execute()
        leftDf.join(rightDf, leftKeys, rightKeys, how, suffix)
    }

    is LogicalPlan.Limit -> {
        val inDf = input.execute()
        val safeOffset = min(max(0, offset), inDf.rowCount)
        val safeLength = min(max(0, length), inDf.rowCount - safeOffset)
        inDf.slice(safeOffset, safeLength)
    }

    is LogicalPlan.Distinct -> {
        val inDf = input.execute()
        val seen = HashSet<GroupKey>()
        val matchingIndices = mutableListOf<Int>()
        val cols = inDf.columns
        for (r in 0 until inDf.rowCount) {
            val key = GroupKey(Array(cols.size) { cols[it][r] })
            if (seen.add(key)) {
                matchingIndices.add(r)
            }
        }
        val sel = SelectionVector(matchingIndices.toIntArray())
        DataFrame(inDf.schema, inDf.columns.map { it.filterWithSelection(sel) })
    }
}
