package io.kodivx.frame

import kotlin.math.min

/**
 * Optimizer rule interface for logical plan transformations.
 */
interface OptimizerRule {
    fun apply(plan: LogicalPlan): LogicalPlan
}

/**
 * Combines adjacent Filter nodes: Filter(Filter(input, p1), p2) -> Filter(input, p1 AND p2).
 */
object CombineFiltersRule : OptimizerRule {
    override fun apply(plan: LogicalPlan): LogicalPlan {
        val optimizedChildren = plan.children.map { apply(it) }
        val newPlan = plan.copyWithChildren(optimizedChildren)

        return if (newPlan is LogicalPlan.Filter && newPlan.input is LogicalPlan.Filter) {
            val innerFilter = newPlan.input
            val combinedPredicate = innerFilter.predicate and newPlan.predicate
            LogicalPlan.Filter(innerFilter.input, combinedPredicate)
        } else {
            newPlan
        }
    }
}

/**
 * Combines adjacent Limit nodes: Limit(Limit(input, 0, l1), 0, l2) -> Limit(input, 0, min(l1, l2)).
 */
object CombineLimitsRule : OptimizerRule {
    override fun apply(plan: LogicalPlan): LogicalPlan {
        val optimizedChildren = plan.children.map { apply(it) }
        val newPlan = plan.copyWithChildren(optimizedChildren)

        return if (newPlan is LogicalPlan.Limit && newPlan.input is LogicalPlan.Limit) {
            val inner = newPlan.input
            if (inner.offset == 0 && newPlan.offset == 0) {
                LogicalPlan.Limit(inner.input, 0, min(inner.length, newPlan.length))
            } else {
                newPlan
            }
        } else {
            newPlan
        }
    }
}

/**
 * Pushes Filter below Projection when predicate only references available source columns.
 */
object PushdownFilterPastProjectionRule : OptimizerRule {
    override fun apply(plan: LogicalPlan): LogicalPlan {
        val optimizedChildren = plan.children.map { apply(it) }
        val newPlan = plan.copyWithChildren(optimizedChildren)

        if (newPlan is LogicalPlan.Filter && newPlan.input is LogicalPlan.Projection) {
            val projection = newPlan.input
            // Check if all expressions in projection are simple ColumnRefs
            val isSimplePassThrough = projection.expressions.all { it is Expr.ColumnRef }
            if (isSimplePassThrough) {
                // Safe to push filter down below projection
                val pushedFilter = LogicalPlan.Filter(projection.input, newPlan.predicate)
                return LogicalPlan.Projection(pushedFilter, projection.expressions)
            }
        }
        return newPlan
    }
}

/**
 * Rule-based query optimizer for [LogicalPlan] trees.
 */
class QueryOptimizer(
    val rules: List<OptimizerRule> = listOf(
        CombineFiltersRule,
        CombineLimitsRule,
        PushdownFilterPastProjectionRule
    )
) {
    fun optimize(plan: LogicalPlan, maxPasses: Int = 5): LogicalPlan {
        var current = plan
        for (i in 0 until maxPasses) {
            var changed = false
            for (rule in rules) {
                val next = rule.apply(current)
                if (next != current) {
                    current = next
                    changed = true
                }
            }
            if (!changed) break
        }
        return current
    }

    companion object {
        val DEFAULT = QueryOptimizer()
    }
}

fun LogicalPlan.optimize(): LogicalPlan = QueryOptimizer.DEFAULT.optimize(this)
