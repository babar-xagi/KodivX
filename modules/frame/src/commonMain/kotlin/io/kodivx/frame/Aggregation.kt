package io.kodivx.frame

/**
 * Represents an aggregation operation applied over grouped rows or entire columns.
 */
sealed interface Aggregation {
    val column: String?
    val outputName: String

    fun alias(name: String): Aggregation

    data class Count(
        override val column: String? = null,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: if (column != null) "count($column)" else "count"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class Sum(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "sum($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class Mean(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "mean($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class Min(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "min($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class Max(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "max($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class Variance(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "var($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }

    data class StdDev(
        override val column: String,
        val customAlias: String? = null
    ) : Aggregation {
        override val outputName: String get() = customAlias ?: "std($column)"
        override fun alias(name: String): Aggregation = copy(customAlias = name)
    }
}

// Top-level builder DSL
fun count(): Aggregation = Aggregation.Count()
fun count(column: String): Aggregation = Aggregation.Count(column)
fun sum(column: String): Aggregation = Aggregation.Sum(column)
fun mean(column: String): Aggregation = Aggregation.Mean(column)
fun min(column: String): Aggregation = Aggregation.Min(column)
fun max(column: String): Aggregation = Aggregation.Max(column)
fun variance(column: String): Aggregation = Aggregation.Variance(column)
fun stdDev(column: String): Aggregation = Aggregation.StdDev(column)
