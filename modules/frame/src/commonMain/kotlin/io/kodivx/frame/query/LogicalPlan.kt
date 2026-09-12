package io.kodivx.frame

import io.kodivx.core.schema.Field
import io.kodivx.core.schema.Schema
import io.kodivx.core.type.DataType

/**
 * Data source abstraction for logical scans.
 */
sealed interface DataSource {
    val schema: Schema
    val description: String

    data class MemorySource(val df: DataFrame) : DataSource {
        override val schema: Schema get() = df.schema
        override val description: String get() = "DataFrame(${df.rowCount} rows, ${df.columnCount} cols)"
    }
}

/**
 * Represents a node in the logical query plan tree.
 */
sealed interface LogicalPlan {
    val schema: Schema
    val children: List<LogicalPlan>

    fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan

    fun toTreeString(): String = buildString {
        appendTree(this@LogicalPlan, "", true)
    }.trimEnd()

    data class Scan(val source: DataSource) : LogicalPlan {
        override val schema: Schema = source.schema
        override val children: List<LogicalPlan> get() = emptyList()
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan = this
    }

    data class Filter(val input: LogicalPlan, val predicate: Expr) : LogicalPlan {
        init {
            predicate.validate(input.schema)
        }

        override val schema: Schema = input.schema
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])
    }

    data class Projection(val input: LogicalPlan, val expressions: List<Expr>) : LogicalPlan {
        override val schema: Schema = Schema(
            expressions.map { expr ->
                val type = expr.inferType(input.schema)
                Field(expr.outputName, type)
            }
        )
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])
    }

    data class Aggregate(
        val input: LogicalPlan,
        val groupKeys: List<String>,
        val aggregations: List<Aggregation>
    ) : LogicalPlan {
        override val schema: Schema = buildSchema()
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])

        private fun buildSchema(): Schema {
            val keyFields = groupKeys.map { input.schema[it] }
            val aggFields = aggregations.map { agg ->
                val type = when (agg) {
                    is Aggregation.Count -> DataType.Int32
                    is Aggregation.Min, is Aggregation.Max -> {
                        val colName = agg.column ?: error("Column required for Min/Max")
                        input.schema[colName].type
                    }
                    else -> DataType.Float64
                }
                Field(agg.outputName, type)
            }
            return Schema(keyFields + aggFields)
        }
    }

    data class Sort(val input: LogicalPlan, val orders: List<SortOrder>) : LogicalPlan {
        override val schema: Schema = input.schema
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])
    }

    data class Join(
        val left: LogicalPlan,
        val right: LogicalPlan,
        val leftKeys: List<String>,
        val rightKeys: List<String>,
        val how: JoinType,
        val suffix: String = "_right"
    ) : LogicalPlan {
        override val schema: Schema = buildSchema()
        override val children: List<LogicalPlan> get() = listOf(left, right)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(left = newChildren[0], right = newChildren[1])

        private fun buildSchema(): Schema {
            if (how == JoinType.Semi || how == JoinType.Anti) {
                return left.schema
            }

            val fields = mutableListOf<Field>()
            fields.addAll(left.schema.fields)

            val sameKeys = (leftKeys == rightKeys)
            val sharedKeySet = if (sameKeys) leftKeys.toSet() else emptySet()

            for (rField in right.schema.fields) {
                if (rField.name in sharedKeySet) continue
                val name = if (left.schema.contains(rField.name)) {
                    "${rField.name}$suffix"
                } else {
                    rField.name
                }
                fields.add(rField.copy(name = name))
            }
            return Schema(fields)
        }
    }

    data class Limit(val input: LogicalPlan, val offset: Int, val length: Int) : LogicalPlan {
        override val schema: Schema = input.schema
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])
    }

    data class Distinct(val input: LogicalPlan) : LogicalPlan {
        override val schema: Schema = input.schema
        override val children: List<LogicalPlan> get() = listOf(input)
        override fun copyWithChildren(newChildren: List<LogicalPlan>): LogicalPlan =
            copy(input = newChildren[0])
    }
}

private fun StringBuilder.appendTree(plan: LogicalPlan, indent: String, isLast: Boolean) {
    val branch = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
    val childIndent = if (indent.isEmpty()) "" else if (isLast) "$indent    " else "$indent│   "

    when (plan) {
        is LogicalPlan.Scan -> {
            append("$indent${branch}Scan: ${plan.source.description} [${plan.schema.fieldNames.joinToString(", ")}]\n")
        }
        is LogicalPlan.Filter -> {
            append("$indent${branch}Filter: ${plan.predicate.toPrettyString()}\n")
            appendTree(plan.input, childIndent, true)
        }
        is LogicalPlan.Projection -> {
            val exprs = plan.expressions.joinToString(", ") { it.toPrettyString() }
            append("$indent${branch}Projection: [$exprs]\n")
            appendTree(plan.input, childIndent, true)
        }
        is LogicalPlan.Aggregate -> {
            val keys = plan.groupKeys.joinToString(", ")
            val aggs = plan.aggregations.joinToString(", ") { it.outputName }
            append("$indent${branch}Aggregate: by [$keys], aggs: [$aggs]\n")
            appendTree(plan.input, childIndent, true)
        }
        is LogicalPlan.Sort -> {
            val orders = plan.orders.joinToString(", ") { "${it.columnName} ${if (it.ascending) "ASC" else "DESC"}" }
            append("$indent${branch}Sort: [$orders]\n")
            appendTree(plan.input, childIndent, true)
        }
        is LogicalPlan.Join -> {
            val lk = plan.leftKeys.joinToString(", ")
            val rk = plan.rightKeys.joinToString(", ")
            append("$indent${branch}Join: ${plan.how} on [$lk] == [$rk]\n")
            appendTree(plan.left, childIndent, false)
            appendTree(plan.right, childIndent, true)
        }
        is LogicalPlan.Limit -> {
            append("$indent${branch}Limit: offset=${plan.offset}, count=${plan.length}\n")
            appendTree(plan.input, childIndent, true)
        }
        is LogicalPlan.Distinct -> {
            append("$indent${branch}Distinct\n")
            appendTree(plan.input, childIndent, true)
        }
    }
}
