package io.kodivx.frame

import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.schema.Schema

/**
 * A lightweight view over a single row in a [DataFrame].
 */
class Row(
    val schema: Schema,
    val columns: List<Column<*>>,
    val rowIndex: Int
) {
    operator fun get(name: String): Any? {
        val col = getColumn(name)
        return col[rowIndex]
    }

    operator fun get(index: Int): Any? = columns[index][rowIndex]

    fun isNull(name: String): Boolean = getColumn(name).isNull(rowIndex)

    fun getInt(name: String): Int {
        val col = getColumn(name)
        if (col is IntColumn) return col.getInt(rowIndex)
        val value = col[rowIndex]
        if (value is Number) return value.toInt()
        throw IllegalStateException("Column '$name' is not an Int column (actual: ${col.type})")
    }

    fun getLong(name: String): Long {
        val col = getColumn(name)
        if (col is LongColumn) return col.getLong(rowIndex)
        val value = col[rowIndex]
        if (value is Number) return value.toLong()
        throw IllegalStateException("Column '$name' is not a Long column (actual: ${col.type})")
    }

    fun getDouble(name: String): Double {
        val col = getColumn(name)
        if (col is DoubleColumn) return col.getDouble(rowIndex)
        val value = col[rowIndex]
        if (value is Number) return value.toDouble()
        throw IllegalStateException("Column '$name' is not a Double column (actual: ${col.type})")
    }

    fun getFloat(name: String): Float {
        val col = getColumn(name)
        if (col is FloatColumn) return col.getFloat(rowIndex)
        val value = col[rowIndex]
        if (value is Number) return value.toFloat()
        throw IllegalStateException("Column '$name' is not a Float column (actual: ${col.type})")
    }

    fun getString(name: String): String? {
        val col = getColumn(name)
        if (col is StringColumn) return col.getString(rowIndex)
        return col[rowIndex]?.toString()
    }

    fun getBoolean(name: String): Boolean {
        val col = getColumn(name)
        if (col is BooleanColumn) return col.getBoolean(rowIndex)
        val value = col[rowIndex]
        if (value is Boolean) return value
        throw IllegalStateException("Column '$name' is not a Boolean column (actual: ${col.type})")
    }

    private fun getColumn(name: String): Column<*> {
        val index = schema.indexOf(name)
        if (index == -1) throw ColumnNotFoundException(name, schema.fieldNames)
        return columns[index]
    }

    override fun toString(): String = buildString {
        append("Row(")
        append(schema.fieldNames.joinToString(", ") { "$it=${get(it)}" })
        append(")")
    }
}
