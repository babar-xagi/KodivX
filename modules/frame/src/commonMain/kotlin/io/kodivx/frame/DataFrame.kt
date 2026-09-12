package io.kodivx.frame

import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.core.error.BufferOutOfBoundsException
import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.schema.Field
import io.kodivx.core.schema.Schema
import kotlin.math.max
import kotlin.math.min

/**
 * Immutable columnar DataFrame representing a tabular dataset of named typed columns.
 */
class DataFrame(
    val schema: Schema,
    val columns: List<Column<*>>
) {
    init {
        require(schema.size == columns.size) {
            "Schema field count (${schema.size}) does not match column count (${columns.size})"
        }
        if (columns.isNotEmpty()) {
            val firstSize = columns[0].size
            for (col in columns) {
                require(col.size == firstSize) {
                    "Column '${col.name}' size (${col.size}) does not match first column size ($firstSize)"
                }
            }
        }
    }

    val rowCount: Int = if (columns.isEmpty()) 0 else columns[0].size
    val columnCount: Int get() = columns.size
    val columnNames: List<String> get() = schema.fieldNames

    operator fun get(name: String): Column<*> {
        val index = schema.indexOf(name)
        if (index == -1) throw ColumnNotFoundException(name, columnNames)
        return columns[index]
    }

    operator fun get(index: Int): Column<*> {
        if (index !in 0 until columnCount) throw BufferOutOfBoundsException(index, columnCount)
        return columns[index]
    }

    fun row(index: Int): Row {
        if (index !in 0 until rowCount) throw BufferOutOfBoundsException(index, rowCount)
        return Row(schema, columns, index)
    }

    // --- Transformations (Immutable) ---

    fun select(vararg names: String): DataFrame = select(names.toList())

    fun select(names: List<String>): DataFrame {
        val selectedFields = mutableListOf<Field>()
        val selectedColumns = mutableListOf<Column<*>>()
        for (name in names) {
            val idx = schema.indexOf(name)
            if (idx == -1) throw ColumnNotFoundException(name, columnNames)
            selectedFields.add(schema[idx])
            selectedColumns.add(columns[idx])
        }
        return DataFrame(Schema(selectedFields), selectedColumns)
    }

    fun withColumn(column: Column<*>): DataFrame = withColumn(column.name, column)

    fun withColumn(name: String, column: Column<*>): DataFrame {
        require(column.size == rowCount || rowCount == 0) {
            "New column '${column.name}' size (${column.size}) does not match DataFrame rowCount ($rowCount)"
        }
        val renamedColumn = if (column.name == name) column else column.withName(name)
        val existingIndex = schema.indexOf(name)

        return if (existingIndex != -1) {
            // Replace existing column
            val newFields = schema.fields.toMutableList().apply {
                this[existingIndex] = renamedColumn.field
            }
            val newColumns = columns.toMutableList().apply {
                this[existingIndex] = renamedColumn
            }
            DataFrame(Schema(newFields), newColumns)
        } else {
            // Append new column
            val newFields = schema.fields + renamedColumn.field
            val newColumns = columns + renamedColumn
            DataFrame(Schema(newFields), newColumns)
        }
    }

    fun drop(vararg names: String): DataFrame {
        val toDrop = names.toSet()
        val keepFields = schema.fields.filterNot { it.name in toDrop }
        val keepColumns = columns.filterNot { it.name in toDrop }
        return DataFrame(Schema(keepFields), keepColumns)
    }

    fun rename(vararg pairs: Pair<String, String>): DataFrame = rename(pairs.toMap())

    fun rename(mapping: Map<String, String>): DataFrame {
        val newFields = mutableListOf<Field>()
        val newColumns = mutableListOf<Column<*>>()
        for (col in columns) {
            val newName = mapping[col.name] ?: col.name
            newFields.add(col.field.copy(name = newName))
            newColumns.add(col.withName(newName))
        }
        return DataFrame(Schema(newFields), newColumns)
    }

    fun limit(n: Int): DataFrame = head(n)

    fun head(n: Int = 5): DataFrame {
        val count = min(max(0, n), rowCount)
        return slice(0, count)
    }

    fun tail(n: Int = 5): DataFrame {
        val count = min(max(0, n), rowCount)
        val offset = rowCount - count
        return slice(offset, count)
    }

    fun slice(offset: Int, length: Int): DataFrame {
        if (offset < 0 || length < 0 || offset + length > rowCount) {
            throw BufferOutOfBoundsException(offset + length, rowCount)
        }
        val slicedColumns = columns.map { it.slice(offset, length) }
        return DataFrame(schema, slicedColumns)
    }

    // --- Filtering with SelectionVector ---

    inline fun filter(predicate: (Row) -> Boolean): DataFrame {
        val matchingIndices = IntArray(rowCount)
        var matchCount = 0

        for (i in 0 until rowCount) {
            val row = Row(schema, columns, i)
            if (predicate(row)) {
                matchingIndices[matchCount++] = i
            }
        }

        val selection = SelectionVector(matchingIndices, 0, matchCount)
        val filteredColumns = columns.map { it.filterWithSelection(selection) }
        return DataFrame(schema, filteredColumns)
    }

    // --- Sorting ---

    fun sortBy(columnName: String, ascending: Boolean = true): DataFrame =
        sortBy(SortOrder(columnName, ascending))

    fun sortBy(vararg orders: SortOrder): DataFrame {
        if (rowCount <= 1) return this
        val rowIndices = Array(rowCount) { it }

        rowIndices.sortWith { r1, r2 ->
            for (order in orders) {
                val col = this[order.columnName]
                val v1 = col[r1]
                val v2 = col[r2]

                if (v1 == null && v2 == null) continue
                if (v1 == null) return@sortWith if (order.nullsFirst) -1 else 1
                if (v2 == null) return@sortWith if (order.nullsFirst) 1 else -1

                @Suppress("UNCHECKED_CAST")
                val cmp = when {
                    v1 is Comparable<*> -> (v1 as Comparable<Any>).compareTo(v2)
                    else -> v1.toString().compareTo(v2.toString())
                }

                if (cmp != 0) {
                    return@sortWith if (order.ascending) cmp else -cmp
                }
            }
            0
        }

        val sortedIndices = IntArray(rowCount) { rowIndices[it] }
        val selection = SelectionVector(sortedIndices)
        val sortedColumns = columns.map { it.filterWithSelection(selection) }
        return DataFrame(schema, sortedColumns)
    }

    // --- Simple Aggregations ---

    fun count(): Int = rowCount

    fun sum(columnName: String): Double {
        val col = this[columnName]
        var total = 0.0
        for (i in 0 until rowCount) {
            val v = col[i]
            if (v is Number) total += v.toDouble()
        }
        return total
    }

    fun mean(columnName: String): Double {
        val col = this[columnName]
        var total = 0.0
        var nonNullCount = 0
        for (i in 0 until rowCount) {
            val v = col[i]
            if (v is Number) {
                total += v.toDouble()
                nonNullCount++
            }
        }
        require(nonNullCount > 0) { "Cannot compute mean: no valid numeric values in column '$columnName'" }
        return total / nonNullCount
    }

    fun min(columnName: String): Double {
        val col = this[columnName]
        var minVal: Double? = null
        for (i in 0 until rowCount) {
            val v = col[i]
            if (v is Number) {
                val d = v.toDouble()
                if (minVal == null || d < minVal) minVal = d
            }
        }
        return minVal ?: throw IllegalStateException("No numeric values in column '$columnName'")
    }

    fun max(columnName: String): Double {
        val col = this[columnName]
        var maxVal: Double? = null
        for (i in 0 until rowCount) {
            val v = col[i]
            if (v is Number) {
                val d = v.toDouble()
                if (maxVal == null || d > maxVal) maxVal = d
            }
        }
        return maxVal ?: throw IllegalStateException("No numeric values in column '$columnName'")
    }

    // --- Tabular ASCII Console Rendering ---

    override fun toString(): String {
        if (columnCount == 0) return "DataFrame(empty, 0 rows)"
        val maxRowsToDisplay = 10
        val rowsToShow = min(rowCount, maxRowsToDisplay)

        // Calculate maximum column widths
        val widths = IntArray(columnCount)
        for (c in 0 until columnCount) {
            val headerLen = columns[c].name.length
            var maxLen = headerLen
            for (r in 0 until rowsToShow) {
                val cellStr = columns[c][r]?.toString() ?: "null"
                if (cellStr.length > maxLen) maxLen = cellStr.length
            }
            widths[c] = min(max(maxLen, 4), 30)
        }

        return buildString {
            append("DataFrame: $rowCount rows x $columnCount cols\n")
            // Headers
            append("┌")
            for (c in 0 until columnCount) {
                append("─".repeat(widths[c] + 2))
                if (c < columnCount - 1) append("┬") else append("┐\n")
            }

            append("│")
            for (c in 0 until columnCount) {
                append(" ")
                append(columns[c].name.padEnd(widths[c]))
                append(" │")
            }
            append("\n")

            append("├")
            for (c in 0 until columnCount) {
                append("─".repeat(widths[c] + 2))
                if (c < columnCount - 1) append("┼") else append("┤\n")
            }

            // Rows
            for (r in 0 until rowsToShow) {
                append("│")
                for (c in 0 until columnCount) {
                    val cell = columns[c][r]
                    val cellStr = if (cell == null) "null" else cell.toString()
                    val truncated = if (cellStr.length > widths[c]) cellStr.take(widths[c] - 3) + "..." else cellStr
                    append(" ")
                    append(truncated.padEnd(widths[c]))
                    append(" │")
                }
                append("\n")
            }

            if (rowCount > maxRowsToDisplay) {
                append("│")
                for (c in 0 until columnCount) {
                    append(" ".padEnd(widths[c] + 2))
                    if (c == columnCount - 1) append("│")
                }
                append(" ... (${rowCount - maxRowsToDisplay} more rows)\n")
            }

            append("└")
            for (c in 0 until columnCount) {
                append("─".repeat(widths[c] + 2))
                if (c < columnCount - 1) append("┴") else append("┘")
            }
        }
    }
}
