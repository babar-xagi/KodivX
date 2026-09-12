package io.kodivx.frame

import io.kodivx.buffer.primitive.*
import io.kodivx.buffer.string.Utf8StringBuffer
import io.kodivx.core.schema.Schema

/**
 * Creates a [DataFrame] from named column pairs.
 *
 * Example:
 * ```kotlin
 * val df = dataFrameOf(
 *     "name" to listOf("Ali", "Sara", "Babar"),
 *     "age" to intArrayOf(21, 24, 23),
 *     "score" to doubleArrayOf(81.0, 95.0, 90.0)
 * )
 * ```
 */
fun dataFrameOf(vararg pairs: Pair<String, Any>): DataFrame {
    val columns = pairs.map { (name, rawValues) ->
        createColumn(name, rawValues)
    }
    val schema = Schema(columns.map { it.field })
    return DataFrame(schema, columns)
}

/**
 * Creates a [DataFrame] directly from an existing list of columns.
 */
fun dataFrameOf(columns: List<Column<*>>): DataFrame {
    val schema = Schema(columns.map { it.field })
    return DataFrame(schema, columns)
}

@Suppress("UNCHECKED_CAST")
private fun createColumn(name: String, raw: Any): Column<*> = when (raw) {
    is Column<*> -> if (raw.name == name) raw else raw.withName(name)
    is IntArray -> IntColumn(name, IntBuffer(raw))
    is LongArray -> LongColumn(name, LongBuffer(raw))
    is DoubleArray -> DoubleColumn(name, DoubleBuffer(raw))
    is FloatArray -> FloatColumn(name, FloatBuffer(raw))
    is BooleanArray -> {
        val wordCount = (raw.size + 63) ushr 6
        val words = LongArray(wordCount)
        raw.forEachIndexed { i, b ->
            if (b) words[i ushr 6] = words[i ushr 6] or (1L shl (i and 63))
        }
        BooleanColumn(name, BooleanBuffer(words, 0, raw.size))
    }
    is List<*> -> createColumnFromList(name, raw)
    is Array<*> -> createColumnFromList(name, raw.toList())
    else -> throw IllegalArgumentException(
        "Unsupported data container type for column '$name': ${raw::class.simpleName}"
    )
}

private fun createColumnFromList(name: String, list: List<*>): Column<*> {
    if (list.isEmpty()) {
        return StringColumn(name, Utf8StringBuffer.of())
    }

    // Find first non-null sample
    val sample = list.firstOrNull { it != null }

    return when (sample) {
        is Int -> {
            @Suppress("UNCHECKED_CAST")
            IntColumn(name, IntBuffer.ofNullable(list as List<Int?>))
        }
        is Long -> {
            @Suppress("UNCHECKED_CAST")
            LongColumn(name, LongBuffer.ofNullable(list as List<Long?>))
        }
        is Double -> {
            @Suppress("UNCHECKED_CAST")
            DoubleColumn(name, DoubleBuffer.ofNullable(list as List<Double?>))
        }
        is Float -> {
            @Suppress("UNCHECKED_CAST")
            FloatColumn(name, FloatBuffer.ofNullable(list as List<Float?>))
        }
        is Boolean -> {
            @Suppress("UNCHECKED_CAST")
            BooleanColumn(name, BooleanBuffer.ofNullable(list as List<Boolean?>))
        }
        else -> {
            // Default to String
            val stringList = list.map { it?.toString() }
            StringColumn(name, Utf8StringBuffer.ofList(stringList))
        }
    }
}
