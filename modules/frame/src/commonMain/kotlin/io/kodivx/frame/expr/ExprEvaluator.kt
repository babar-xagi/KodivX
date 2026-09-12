package io.kodivx.frame

import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.buffer.bitmap.ValidityBitmapBuilder
import io.kodivx.buffer.builder.Utf8StringBufferBuilder
import io.kodivx.buffer.primitive.*
import io.kodivx.buffer.string.Utf8StringBuffer
import io.kodivx.core.type.DataType
import io.kodivx.core.type.Scalar
import io.kodivx.frame.*

/**
 * Evaluates this [Expr] across all rows of [df], returning a typed [Column].
 */
fun Expr.eval(df: DataFrame): Column<*> = when (this) {
    is Expr.Literal -> evalLiteral(scalar, df.rowCount, outputName)

    is Expr.ColumnRef -> {
        val col = df[name]
        if (explicitType != null && col.type != explicitType) {
            evalCast(col, explicitType, outputName)
        } else {
            col
        }
    }

    is Expr.Alias -> expr.eval(df).withName(aliasName)

    is Expr.Cast -> evalCast(expr.eval(df), targetType, outputName)

    is Expr.UnaryMinus -> evalUnaryMinus(expr.eval(df), outputName)

    is Expr.Not -> evalNot(expr.eval(df), outputName)

    is Expr.IsNull -> evalIsNull(expr.eval(df), outputName)

    is Expr.IsNotNull -> evalIsNotNull(expr.eval(df), outputName)

    is Expr.Add -> evalAdd(left.eval(df), right.eval(df), outputName)

    is Expr.Subtract -> evalBinaryArithmetic(left.eval(df), right.eval(df), outputName) { a, b -> a - b }

    is Expr.Multiply -> evalBinaryArithmetic(left.eval(df), right.eval(df), outputName) { a, b -> a * b }

    is Expr.Divide -> evalBinaryArithmetic(left.eval(df), right.eval(df), outputName) { a, b -> a / b }

    is Expr.Modulo -> evalBinaryArithmetic(left.eval(df), right.eval(df), outputName) { a, b -> a % b }

    is Expr.Equal -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp == 0 }

    is Expr.NotEqual -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp != 0 }

    is Expr.GreaterThan -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp > 0 }

    is Expr.GreaterOrEqual -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp >= 0 }

    is Expr.LessThan -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp < 0 }

    is Expr.LessOrEqual -> evalComparison(left.eval(df), right.eval(df), outputName) { cmp -> cmp <= 0 }

    is Expr.And -> evalAnd(left.eval(df), right.eval(df), outputName)

    is Expr.Or -> evalOr(left.eval(df), right.eval(df), outputName)

    is Expr.Aggregate -> error("Scalar evaluation of Aggregate expression not supported directly; use groupBy().agg()")

    is Aggregation -> error("Scalar evaluation of Aggregation expression not supported directly; use groupBy().agg()")

    is Expr.Contains -> evalStringPredicate(expr.eval(df), outputName) { it.contains(pattern) }

    is Expr.StartsWith -> evalStringPredicate(expr.eval(df), outputName) { it.startsWith(prefix) }

    is Expr.EndsWith -> evalStringPredicate(expr.eval(df), outputName) { it.endsWith(suffix) }

    is Expr.Lower -> evalStringTransform(expr.eval(df), outputName) { it.lowercase() }

    is Expr.Upper -> evalStringTransform(expr.eval(df), outputName) { it.uppercase() }

    is Expr.Length -> evalStringLength(expr.eval(df), outputName)
}

// --- Literal Column Creation ---

private fun evalLiteral(scalar: Scalar, rowCount: Int, name: String): Column<*> = when (scalar) {
    is Scalar.Int -> IntColumn(name, IntBuffer(IntArray(rowCount) { scalar.value }))
    is Scalar.Long -> LongColumn(name, LongBuffer(LongArray(rowCount) { scalar.value }))
    is Scalar.Double -> DoubleColumn(name, DoubleBuffer(DoubleArray(rowCount) { scalar.value }))
    is Scalar.Float -> FloatColumn(name, FloatBuffer(FloatArray(rowCount) { scalar.value }))
    is Scalar.Bool -> {
        val wordCount = (rowCount + 63) ushr 6
        val words = LongArray(wordCount) { if (scalar.value) -1L else 0L }
        BooleanColumn(name, BooleanBuffer(words, 0, rowCount))
    }
    is Scalar.Utf8 -> {
        val builder = Utf8StringBufferBuilder(rowCount)
        repeat(rowCount) { builder.append(scalar.value) }
        StringColumn(name, builder.build())
    }
    is Scalar.Null -> {
        val vBuilder = ValidityBitmapBuilder(rowCount)
        repeat(rowCount) { vBuilder.appendNull() }
        val v = vBuilder.build()
        when (scalar.type) {
            DataType.Int32 -> IntColumn(name, IntBuffer(IntArray(rowCount), 0, rowCount, v))
            DataType.Int64 -> LongColumn(name, LongBuffer(LongArray(rowCount), 0, rowCount, v))
            DataType.Float32 -> FloatColumn(name, FloatBuffer(FloatArray(rowCount), 0, rowCount, v))
            DataType.Boolean -> BooleanColumn(name, BooleanBuffer(LongArray((rowCount + 63) ushr 6), 0, rowCount, v))
            DataType.Utf8 -> StringColumn(name, Utf8StringBuffer(ByteArray(0), IntArray(rowCount + 1), 0, rowCount, v))
            else -> DoubleColumn(name, DoubleBuffer(DoubleArray(rowCount), 0, rowCount, v))
        }
    }
    else -> error("Unsupported scalar literal: $scalar")
}

// --- Casting ---

private fun evalCast(col: Column<*>, targetType: DataType, name: String): Column<*> {
    val size = col.size
    return when (targetType) {
        DataType.Int32 -> {
            val data = IntArray(size)
            val vBuilder = ValidityBitmapBuilder(size)
            for (i in 0 until size) {
                if (col.isValid(i)) {
                    val v = col[i]
                    if (v is Number) {
                        data[i] = v.toInt()
                        vBuilder.appendValid()
                        continue
                    }
                }
                vBuilder.appendNull()
            }
            IntColumn(name, IntBuffer(data, 0, size, vBuilder.build()))
        }
        DataType.Int64 -> {
            val data = LongArray(size)
            val vBuilder = ValidityBitmapBuilder(size)
            for (i in 0 until size) {
                if (col.isValid(i)) {
                    val v = col[i]
                    if (v is Number) {
                        data[i] = v.toLong()
                        vBuilder.appendValid()
                        continue
                    }
                }
                vBuilder.appendNull()
            }
            LongColumn(name, LongBuffer(data, 0, size, vBuilder.build()))
        }
        DataType.Float64 -> {
            val data = DoubleArray(size)
            val vBuilder = ValidityBitmapBuilder(size)
            for (i in 0 until size) {
                if (col.isValid(i)) {
                    val v = col[i]
                    if (v is Number) {
                        data[i] = v.toDouble()
                        vBuilder.appendValid()
                        continue
                    }
                }
                vBuilder.appendNull()
            }
            DoubleColumn(name, DoubleBuffer(data, 0, size, vBuilder.build()))
        }
        DataType.Float32 -> {
            val data = FloatArray(size)
            val vBuilder = ValidityBitmapBuilder(size)
            for (i in 0 until size) {
                if (col.isValid(i)) {
                    val v = col[i]
                    if (v is Number) {
                        data[i] = v.toFloat()
                        vBuilder.appendValid()
                        continue
                    }
                }
                vBuilder.appendNull()
            }
            FloatColumn(name, FloatBuffer(data, 0, size, vBuilder.build()))
        }
        DataType.Utf8 -> {
            val builder = Utf8StringBufferBuilder(size)
            for (i in 0 until size) {
                if (col.isValid(i)) {
                    builder.append(col[i]?.toString() ?: "")
                } else {
                    builder.appendNull()
                }
            }
            StringColumn(name, builder.build())
        }
        else -> error("Unsupported cast target type: $targetType")
    }
}

// --- Unary Operators ---

private fun evalUnaryMinus(col: Column<*>, name: String): Column<*> {
    val size = col.size
    return when (col) {
        is IntColumn -> {
            val data = IntArray(size) { if (col.isValid(it)) -col.getInt(it) else 0 }
            IntColumn(name, IntBuffer(data, 0, size, col.buffer.validity))
        }
        is LongColumn -> {
            val data = LongArray(size) { if (col.isValid(it)) -col.getLong(it) else 0L }
            LongColumn(name, LongBuffer(data, 0, size, col.buffer.validity))
        }
        is FloatColumn -> {
            val data = FloatArray(size) { if (col.isValid(it)) -col.getFloat(it) else 0.0f }
            FloatColumn(name, FloatBuffer(data, 0, size, col.buffer.validity))
        }
        else -> {
            val data = DoubleArray(size) {
                val v = col[it]
                if (v is Number && col.isValid(it)) -v.toDouble() else 0.0
            }
            DoubleColumn(name, DoubleBuffer(data, 0, size, col.buffer.validity))
        }
    }
}

private fun evalNot(col: Column<*>, name: String): Column<*> {
    require(col is BooleanColumn) { "NOT operator requires BooleanColumn, got: ${col::class.simpleName}" }
    val size = col.size
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (w in 0 until wordCount) {
        outWords[w] = col.buffer.words[w].inv()
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size, col.buffer.validity))
}

private fun evalIsNull(col: Column<*>, name: String): Column<*> {
    val size = col.size
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (i in 0 until size) {
        if (col.isNull(i)) {
            outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
        }
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size))
}

private fun evalIsNotNull(col: Column<*>, name: String): Column<*> {
    val size = col.size
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (i in 0 until size) {
        if (col.isValid(i)) {
            outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
        }
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size))
}

// --- Binary Arithmetic ---

private fun evalAdd(left: Column<*>, right: Column<*>, name: String): Column<*> {
    val size = left.size
    if (left is StringColumn || right is StringColumn) {
        val builder = Utf8StringBufferBuilder(size)
        for (i in 0 until size) {
            if (left.isValid(i) && right.isValid(i)) {
                builder.append("${left[i]}${right[i]}")
            } else {
                builder.appendNull()
            }
        }
        return StringColumn(name, builder.build())
    }
    return evalBinaryArithmetic(left, right, name) { a, b -> a + b }
}

private inline fun evalBinaryArithmetic(
    left: Column<*>,
    right: Column<*>,
    name: String,
    crossinline op: (Double, Double) -> Double
): Column<*> {
    val size = left.size
    val validity = combineValidity(left.buffer.validity, right.buffer.validity, size)

    // Fast-path for Int + Int
    if (left is IntColumn && right is IntColumn) {
        val outData = IntArray(size)
        for (i in 0 until size) {
            if (validity.isValid(i)) {
                outData[i] = op(left.getInt(i).toDouble(), right.getInt(i).toDouble()).toInt()
            }
        }
        return IntColumn(name, IntBuffer(outData, 0, size, validity))
    }

    // Default numeric Float64 path
    val outData = DoubleArray(size)
    for (i in 0 until size) {
        if (validity.isValid(i)) {
            val lVal = (left[i] as Number).toDouble()
            val rVal = (right[i] as Number).toDouble()
            outData[i] = op(lVal, rVal)
        }
    }
    return DoubleColumn(name, DoubleBuffer(outData, 0, size, validity))
}

// --- Comparisons ---

private inline fun evalComparison(
    left: Column<*>,
    right: Column<*>,
    name: String,
    crossinline predicate: (Int) -> Boolean
): Column<*> {
    val size = left.size
    val validity = combineValidity(left.buffer.validity, right.buffer.validity, size)
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)

    // Fast-path: Double vs Double
    if (left is DoubleColumn && right is DoubleColumn) {
        for (i in 0 until size) {
            if (validity.isValid(i)) {
                val cmp = left.getDouble(i).compareTo(right.getDouble(i))
                if (predicate(cmp)) {
                    outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
                }
            }
        }
        return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity))
    }

    // Fast-path: Int vs Int
    if (left is IntColumn && right is IntColumn) {
        for (i in 0 until size) {
            if (validity.isValid(i)) {
                val cmp = left.getInt(i).compareTo(right.getInt(i))
                if (predicate(cmp)) {
                    outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
                }
            }
        }
        return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity))
    }

    // General Comparable fallback
    for (i in 0 until size) {
        if (validity.isValid(i)) {
            val lVal = left[i]
            val rVal = right[i]
            val cmp = when {
                lVal is Number && rVal is Number -> lVal.toDouble().compareTo(rVal.toDouble())
                lVal is Comparable<*> && rVal != null -> {
                    @Suppress("UNCHECKED_CAST")
                    (lVal as Comparable<Any>).compareTo(rVal)
                }
                else -> lVal.toString().compareTo(rVal.toString())
            }
            if (predicate(cmp)) {
                outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
            }
        }
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity))
}

// --- Logical AND / OR ---

private fun evalAnd(left: Column<*>, right: Column<*>, name: String): Column<*> {
    require(left is BooleanColumn && right is BooleanColumn) { "AND requires BooleanColumns" }
    val size = left.size
    val validity = combineValidity(left.buffer.validity, right.buffer.validity, size)
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (w in 0 until wordCount) {
        outWords[w] = left.buffer.words[w] and right.buffer.words[w]
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity))
}

private fun evalOr(left: Column<*>, right: Column<*>, name: String): Column<*> {
    require(left is BooleanColumn && right is BooleanColumn) { "OR requires BooleanColumns" }
    val size = left.size
    val validity = combineValidity(left.buffer.validity, right.buffer.validity, size)
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (w in 0 until wordCount) {
        outWords[w] = left.buffer.words[w] or right.buffer.words[w]
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity))
}

// --- String Helpers ---

private inline fun evalStringPredicate(
    col: Column<*>,
    name: String,
    crossinline predicate: (String) -> Boolean
): Column<*> {
    val size = col.size
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    val validity = col.buffer.validity

    for (i in 0 until size) {
        if (validity == null || validity.isValid(i)) {
            val s = col[i]?.toString() ?: ""
            if (predicate(s)) {
                outWords[i ushr 6] = outWords[i ushr 6] or (1L shl (i and 63))
            }
        }
    }
    return BooleanColumn(name, BooleanBuffer(outWords, 0, size, validity ?: ValidityBitmap.allValid(size)))
}

private inline fun evalStringTransform(
    col: Column<*>,
    name: String,
    crossinline transform: (String) -> String
): Column<*> {
    val size = col.size
    val builder = Utf8StringBufferBuilder(size)
    for (i in 0 until size) {
        if (col.isValid(i)) {
            val s = col[i]?.toString() ?: ""
            builder.append(transform(s))
        } else {
            builder.appendNull()
        }
    }
    return StringColumn(name, builder.build())
}

private fun evalStringLength(col: Column<*>, name: String): Column<*> {
    val size = col.size
    val data = IntArray(size)
    val validity = col.buffer.validity
    for (i in 0 until size) {
        if (validity == null || validity.isValid(i)) {
            data[i] = col[i]?.toString()?.length ?: 0
        }
    }
    return IntColumn(name, IntBuffer(data, 0, size, validity ?: ValidityBitmap.allValid(size)))
}

// --- Validity Combination SWAR ---

private fun combineValidity(v1: ValidityBitmap?, v2: ValidityBitmap?, size: Int): ValidityBitmap {
    if (v1 == null && v2 == null) return ValidityBitmap.allValid(size)
    if (v1 == null) return v2!!
    if (v2 == null) return v1
    val wordCount = (size + 63) ushr 6
    val outWords = LongArray(wordCount)
    for (w in 0 until wordCount) {
        val w1 = if (w < v1.words.size) v1.words[w] else -1L
        val w2 = if (w < v2.words.size) v2.words[w] else -1L
        outWords[w] = w1 and w2
    }
    return ValidityBitmap(outWords, size)
}
