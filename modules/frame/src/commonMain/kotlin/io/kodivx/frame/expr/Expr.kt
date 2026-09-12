package io.kodivx.frame

import io.kodivx.core.type.DataType
import io.kodivx.core.type.Scalar

/**
 * Represents an Abstract Syntax Tree (AST) node for columnar expressions in KodivX.
 */
sealed interface Expr {
    val outputName: String

    // --- Core AST Nodes ---

    data class Literal(val scalar: Scalar) : Expr {
        override val outputName: String get() = scalar.toString()
    }

    data class ColumnRef(val name: String, val explicitType: DataType? = null) : Expr {
        override val outputName: String get() = name
    }

    data class Alias(val expr: Expr, val aliasName: String) : Expr {
        override val outputName: String get() = aliasName
    }

    data class Cast(val expr: Expr, val targetType: DataType) : Expr {
        override val outputName: String get() = "cast(${expr.outputName} as ${targetType.displayName})"
    }

    data class UnaryMinus(val expr: Expr) : Expr {
        override val outputName: String get() = "-(${expr.outputName})"
    }

    data class Not(val expr: Expr) : Expr {
        override val outputName: String get() = "not(${expr.outputName})"
    }

    data class IsNull(val expr: Expr) : Expr {
        override val outputName: String get() = "(${expr.outputName} is null)"
    }

    data class IsNotNull(val expr: Expr) : Expr {
        override val outputName: String get() = "(${expr.outputName} is not null)"
    }

    // --- Binary Arithmetic ---

    data class Add(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} + ${right.outputName})"
    }

    data class Subtract(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} - ${right.outputName})"
    }

    data class Multiply(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} * ${right.outputName})"
    }

    data class Divide(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} / ${right.outputName})"
    }

    data class Modulo(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} % ${right.outputName})"
    }

    // --- Binary Comparisons ---

    data class Equal(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} == ${right.outputName})"
    }

    data class NotEqual(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} != ${right.outputName})"
    }

    data class GreaterThan(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} > ${right.outputName})"
    }

    data class GreaterOrEqual(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} >= ${right.outputName})"
    }

    data class LessThan(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} < ${right.outputName})"
    }

    data class LessOrEqual(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} <= ${right.outputName})"
    }

    // --- Logical Operations ---

    data class And(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} and ${right.outputName})"
    }

    data class Or(val left: Expr, val right: Expr) : Expr {
        override val outputName: String get() = "(${left.outputName} or ${right.outputName})"
    }

    // --- Aggregations ---

    enum class AggKind {
        Count,
        Sum,
        Mean,
        Min,
        Max,
        Variance,
        StdDev
    }

    data class Aggregate(val kind: AggKind, val expr: Expr?) : Expr {
        override val outputName: String get() = "${kind.name.lowercase()}(${expr?.outputName ?: "*"})"
    }

    // --- String Operations ---

    data class Contains(val expr: Expr, val pattern: String) : Expr {
        override val outputName: String get() = "contains(${expr.outputName}, \"$pattern\")"
    }

    data class StartsWith(val expr: Expr, val prefix: String) : Expr {
        override val outputName: String get() = "startsWith(${expr.outputName}, \"$prefix\")"
    }

    data class EndsWith(val expr: Expr, val suffix: String) : Expr {
        override val outputName: String get() = "endsWith(${expr.outputName}, \"$suffix\")"
    }

    data class Lower(val expr: Expr) : Expr {
        override val outputName: String get() = "lower(${expr.outputName})"
    }

    data class Upper(val expr: Expr) : Expr {
        override val outputName: String get() = "upper(${expr.outputName})"
    }

    data class Length(val expr: Expr) : Expr {
        override val outputName: String get() = "length(${expr.outputName})"
    }
}

// --- Top-Level DSL Builders ---

@kotlin.jvm.JvmName("colUntyped")
fun col(name: String): Expr.ColumnRef = Expr.ColumnRef(name)

@kotlin.jvm.JvmName("colTyped")
inline fun <reified T> col(name: String): Expr.ColumnRef {
    val explicitType = when (T::class) {
        Int::class -> DataType.Int32
        Long::class -> DataType.Int64
        Double::class -> DataType.Float64
        Float::class -> DataType.Float32
        Boolean::class -> DataType.Boolean
        String::class -> DataType.Utf8
        ByteArray::class -> DataType.Binary
        else -> null
    }
    return Expr.ColumnRef(name, explicitType)
}

fun lit(value: Int): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: Long): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: Double): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: Float): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: Boolean): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: String): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(value: ByteArray): Expr.Literal = Expr.Literal(Scalar.of(value))
fun lit(scalar: Scalar): Expr.Literal = Expr.Literal(scalar)
fun litNull(type: DataType): Expr.Literal = Expr.Literal(Scalar.nullOf(type))

// --- Operator Overloads & Extension Functions on Expr ---

operator fun Expr.plus(other: Expr): Expr = Expr.Add(this, other)
operator fun Expr.plus(other: Number): Expr = Expr.Add(this, toLiteralNumber(other))
operator fun Expr.plus(other: String): Expr = Expr.Add(this, lit(other))

operator fun Expr.minus(other: Expr): Expr = Expr.Subtract(this, other)
operator fun Expr.minus(other: Number): Expr = Expr.Subtract(this, toLiteralNumber(other))

operator fun Expr.times(other: Expr): Expr = Expr.Multiply(this, other)
operator fun Expr.times(other: Number): Expr = Expr.Multiply(this, toLiteralNumber(other))

operator fun Expr.div(other: Expr): Expr = Expr.Divide(this, other)
operator fun Expr.div(other: Number): Expr = Expr.Divide(this, toLiteralNumber(other))

operator fun Expr.rem(other: Expr): Expr = Expr.Modulo(this, other)
operator fun Expr.rem(other: Number): Expr = Expr.Modulo(this, toLiteralNumber(other))

operator fun Expr.unaryMinus(): Expr = Expr.UnaryMinus(this)
operator fun Expr.not(): Expr = Expr.Not(this)

infix fun Expr.and(other: Expr): Expr = Expr.And(this, other)
infix fun Expr.or(other: Expr): Expr = Expr.Or(this, other)

infix fun Expr.eq(other: Expr): Expr = Expr.Equal(this, other)
infix fun Expr.eq(other: Any?): Expr = Expr.Equal(this, toLiteralAny(other))

infix fun Expr.neq(other: Expr): Expr = Expr.NotEqual(this, other)
infix fun Expr.neq(other: Any?): Expr = Expr.NotEqual(this, toLiteralAny(other))

infix fun Expr.gt(other: Expr): Expr = Expr.GreaterThan(this, other)
infix fun Expr.gt(other: Number): Expr = Expr.GreaterThan(this, toLiteralNumber(other))
infix fun Expr.gt(other: String): Expr = Expr.GreaterThan(this, lit(other))

infix fun Expr.gte(other: Expr): Expr = Expr.GreaterOrEqual(this, other)
infix fun Expr.gte(other: Number): Expr = Expr.GreaterOrEqual(this, toLiteralNumber(other))
infix fun Expr.gte(other: String): Expr = Expr.GreaterOrEqual(this, lit(other))

infix fun Expr.lt(other: Expr): Expr = Expr.LessThan(this, other)
infix fun Expr.lt(other: Number): Expr = Expr.LessThan(this, toLiteralNumber(other))
infix fun Expr.lt(other: String): Expr = Expr.LessThan(this, lit(other))

infix fun Expr.lte(other: Expr): Expr = Expr.LessOrEqual(this, other)
infix fun Expr.lte(other: Number): Expr = Expr.LessOrEqual(this, toLiteralNumber(other))
infix fun Expr.lte(other: String): Expr = Expr.LessOrEqual(this, lit(other))

fun Expr.isNull(): Expr = Expr.IsNull(this)
fun Expr.isNotNull(): Expr = Expr.IsNotNull(this)
fun Expr.cast(targetType: DataType): Expr = Expr.Cast(this, targetType)

infix fun Expr.asAlias(name: String): Expr = Expr.Alias(this, name)
fun Expr.alias(name: String): Expr = Expr.Alias(this, name)

// Reverse operators for Number on LHS
operator fun Number.plus(expr: Expr): Expr = Expr.Add(toLiteralNumber(this), expr)
operator fun Number.minus(expr: Expr): Expr = Expr.Subtract(toLiteralNumber(this), expr)
operator fun Number.times(expr: Expr): Expr = Expr.Multiply(toLiteralNumber(this), expr)
operator fun Number.div(expr: Expr): Expr = Expr.Divide(toLiteralNumber(this), expr)
operator fun Number.rem(expr: Expr): Expr = Expr.Modulo(toLiteralNumber(this), expr)

// String expression helpers
fun Expr.contains(pattern: String): Expr = Expr.Contains(this, pattern)
fun Expr.startsWith(prefix: String): Expr = Expr.StartsWith(this, prefix)
fun Expr.endsWith(suffix: String): Expr = Expr.EndsWith(this, suffix)
fun Expr.lower(): Expr = Expr.Lower(this)
fun Expr.upper(): Expr = Expr.Upper(this)
fun Expr.length(): Expr = Expr.Length(this)

// Aggregate expressions
fun count(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Count, expr)
fun sum(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Sum, expr)
fun mean(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Mean, expr)
fun min(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Min, expr)
fun max(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Max, expr)
fun variance(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.Variance, expr)
fun stdDev(expr: Expr): Expr = Expr.Aggregate(Expr.AggKind.StdDev, expr)

// Internal literal conversion helpers
internal fun toLiteralNumber(num: Number): Expr.Literal = when (num) {
    is Int -> Expr.Literal(Scalar.of(num))
    is Long -> Expr.Literal(Scalar.of(num))
    is Double -> Expr.Literal(Scalar.of(num))
    is Float -> Expr.Literal(Scalar.of(num))
    is Short -> Expr.Literal(Scalar.of(num.toInt()))
    is Byte -> Expr.Literal(Scalar.of(num.toInt()))
    else -> Expr.Literal(Scalar.of(num.toDouble()))
}

internal fun toLiteralAny(value: Any?): Expr.Literal = when (value) {
    null -> Expr.Literal(Scalar.nullOf(DataType.Utf8))
    is Number -> toLiteralNumber(value)
    is Boolean -> Expr.Literal(Scalar.of(value))
    is String -> Expr.Literal(Scalar.of(value))
    is ByteArray -> Expr.Literal(Scalar.of(value))
    is Scalar -> Expr.Literal(value)
    is Expr.Literal -> value
    else -> Expr.Literal(Scalar.of(value.toString()))
}
