package io.kodivx.frame

import io.kodivx.buffer.bitmap.ValidityBitmapBuilder
import io.kodivx.buffer.builder.Utf8StringBufferBuilder
import io.kodivx.buffer.primitive.*
import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.core.error.ColumnNotFoundException

enum class JoinType {
    Inner,
    Left,
    Right,
    Full,
    Semi,
    Anti
}

fun DataFrame.join(
    other: DataFrame,
    on: String,
    how: JoinType = JoinType.Inner,
    suffix: String = "_right"
): DataFrame = join(other, listOf(on), listOf(on), how, suffix)

fun DataFrame.join(
    other: DataFrame,
    leftOn: String,
    rightOn: String,
    how: JoinType = JoinType.Inner,
    suffix: String = "_right"
): DataFrame = join(other, listOf(leftOn), listOf(rightOn), how, suffix)

fun DataFrame.join(
    other: DataFrame,
    leftOn: List<String>,
    rightOn: List<String>,
    how: JoinType = JoinType.Inner,
    suffix: String = "_right"
): DataFrame {
    require(leftOn.isNotEmpty()) { "At least one join key must be specified" }
    require(leftOn.size == rightOn.size) {
        "leftOn (${leftOn.size} keys) and rightOn (${rightOn.size} keys) must have the same length"
    }

    for (k in leftOn) {
        if (schema.indexOf(k) == -1) throw ColumnNotFoundException(k, columnNames)
    }
    for (k in rightOn) {
        if (other.schema.indexOf(k) == -1) throw ColumnNotFoundException(k, other.columnNames)
    }

    // 1. Build hash table on the right DataFrame
    val rightKeyCols = rightOn.map { other[it] }
    val rightIndex = LinkedHashMap<GroupKey, MutableList<Int>>()
    for (r in 0 until other.rowCount) {
        val keyArr = Array<Any?>(rightKeyCols.size) { colIdx -> rightKeyCols[colIdx][r] }
        val key = GroupKey(keyArr)
        rightIndex.getOrPut(key) { mutableListOf() }.add(r)
    }

    val leftKeyCols = leftOn.map { this[it] }

    // Semi and Anti joins only return rows from left table
    when (how) {
        JoinType.Semi -> {
            val matchingLeftRows = mutableListOf<Int>()
            for (l in 0 until rowCount) {
                val key = GroupKey(Array(leftKeyCols.size) { colIdx -> leftKeyCols[colIdx][l] })
                if (rightIndex.containsKey(key)) {
                    matchingLeftRows.add(l)
                }
            }
            val selection = SelectionVector(matchingLeftRows.toIntArray())
            val outCols = columns.map { it.filterWithSelection(selection) }
            return DataFrame(schema, outCols)
        }
        JoinType.Anti -> {
            val matchingLeftRows = mutableListOf<Int>()
            for (l in 0 until rowCount) {
                val key = GroupKey(Array(leftKeyCols.size) { colIdx -> leftKeyCols[colIdx][l] })
                if (!rightIndex.containsKey(key)) {
                    matchingLeftRows.add(l)
                }
            }
            val selection = SelectionVector(matchingLeftRows.toIntArray())
            val outCols = columns.map { it.filterWithSelection(selection) }
            return DataFrame(schema, outCols)
        }
        JoinType.Inner, JoinType.Left, JoinType.Right, JoinType.Full -> {
            // Collect matching row index pairs
            val leftIndicesList = mutableListOf<Int>()
            val rightIndicesList = mutableListOf<Int>()
            val matchedRightRows = if (how == JoinType.Right || how == JoinType.Full) {
                BooleanArray(other.rowCount)
            } else null

            for (l in 0 until rowCount) {
                val key = GroupKey(Array(leftKeyCols.size) { colIdx -> leftKeyCols[colIdx][l] })
                val matches = rightIndex[key]
                if (matches != null && matches.isNotEmpty()) {
                    for (r in matches) {
                        leftIndicesList.add(l)
                        rightIndicesList.add(r)
                        if (matchedRightRows != null) matchedRightRows[r] = true
                    }
                } else if (how == JoinType.Left || how == JoinType.Full) {
                    leftIndicesList.add(l)
                    rightIndicesList.add(-1)
                }
            }

            if (how == JoinType.Right || how == JoinType.Full) {
                for (r in 0 until other.rowCount) {
                    if (!matchedRightRows!![r]) {
                        leftIndicesList.add(-1)
                        rightIndicesList.add(r)
                    }
                }
            }

            val leftIndices = leftIndicesList.toIntArray()
            val rightIndices = rightIndicesList.toIntArray()

            val sameKeys = (leftOn == rightOn)
            val sharedKeySet = if (sameKeys) leftOn.toSet() else emptySet()

            val resultColumns = mutableListOf<Column<*>>()

            // Add left columns
            for (lCol in columns) {
                if (lCol.name in sharedKeySet) {
                    val rCol = other[lCol.name]
                    resultColumns.add(
                        remapCoalescedKeyColumn(lCol, rCol, leftIndices, rightIndices, lCol.name)
                    )
                } else {
                    resultColumns.add(
                        remapColumn(lCol, leftIndices, lCol.name)
                    )
                }
            }

            // Add right columns
            for (rCol in other.columns) {
                if (rCol.name in sharedKeySet) {
                    // Already coalesced with left column
                    continue
                }
                val outName = if (schema.indexOf(rCol.name) != -1) {
                    "${rCol.name}$suffix"
                } else {
                    rCol.name
                }
                resultColumns.add(
                    remapColumn(rCol, rightIndices, outName)
                )
            }

            return dataFrameOf(resultColumns)
        }
    }
}

/**
 * Materializes a [Column] by reindexing according to [indices].
 * An index value of `-1` indicates a NULL entry.
 */
internal fun remapColumn(column: Column<*>, indices: IntArray, newName: String): Column<*> = when (column) {
    is IntColumn -> {
        val data = IntArray(indices.size)
        val vBuilder = ValidityBitmapBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                vBuilder.appendNull()
                data[i] = 0
            } else {
                vBuilder.appendValid()
                data[i] = column.getInt(idx)
            }
        }
        IntColumn(newName, IntBuffer(data, 0, indices.size, vBuilder.build()))
    }
    is LongColumn -> {
        val data = LongArray(indices.size)
        val vBuilder = ValidityBitmapBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                vBuilder.appendNull()
                data[i] = 0L
            } else {
                vBuilder.appendValid()
                data[i] = column.getLong(idx)
            }
        }
        LongColumn(newName, LongBuffer(data, 0, indices.size, vBuilder.build()))
    }
    is DoubleColumn -> {
        val data = DoubleArray(indices.size)
        val vBuilder = ValidityBitmapBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                vBuilder.appendNull()
                data[i] = 0.0
            } else {
                vBuilder.appendValid()
                data[i] = column.getDouble(idx)
            }
        }
        DoubleColumn(newName, DoubleBuffer(data, 0, indices.size, vBuilder.build()))
    }
    is FloatColumn -> {
        val data = FloatArray(indices.size)
        val vBuilder = ValidityBitmapBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                vBuilder.appendNull()
                data[i] = 0.0f
            } else {
                vBuilder.appendValid()
                data[i] = column.getFloat(idx)
            }
        }
        FloatColumn(newName, FloatBuffer(data, 0, indices.size, vBuilder.build()))
    }
    is BooleanColumn -> {
        val wordCount = (indices.size + 63) ushr 6
        val words = LongArray(wordCount)
        val vBuilder = ValidityBitmapBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                vBuilder.appendNull()
            } else {
                vBuilder.appendValid()
                if (column.getBoolean(idx)) {
                    words[i ushr 6] = words[i ushr 6] or (1L shl (i and 63))
                }
            }
        }
        BooleanColumn(newName, BooleanBuffer(words, 0, indices.size, vBuilder.build()))
    }
    is StringColumn -> {
        val builder = Utf8StringBufferBuilder(indices.size)
        for (i in indices.indices) {
            val idx = indices[i]
            if (idx == -1 || column.isNull(idx)) {
                builder.appendNull()
            } else {
                val str = column.getString(idx)
                if (str != null) builder.append(str) else builder.appendNull()
            }
        }
        StringColumn(newName, builder.build())
    }
    else -> throw IllegalArgumentException("Unsupported column type: ${column::class.simpleName}")
}

/**
 * Materializes a key column for Full/Right joins coalescing left and right key values.
 */
private fun remapCoalescedKeyColumn(
    leftCol: Column<*>,
    rightCol: Column<*>,
    leftIndices: IntArray,
    rightIndices: IntArray,
    newName: String
): Column<*> = when {
    leftCol is IntColumn && rightCol is IntColumn -> {
        val data = IntArray(leftIndices.size)
        val vBuilder = ValidityBitmapBuilder(leftIndices.size)
        for (i in leftIndices.indices) {
            val lIdx = leftIndices[i]
            val rIdx = rightIndices[i]
            if (lIdx != -1 && leftCol.isValid(lIdx)) {
                vBuilder.appendValid()
                data[i] = leftCol.getInt(lIdx)
            } else if (rIdx != -1 && rightCol.isValid(rIdx)) {
                vBuilder.appendValid()
                data[i] = rightCol.getInt(rIdx)
            } else {
                vBuilder.appendNull()
                data[i] = 0
            }
        }
        IntColumn(newName, IntBuffer(data, 0, leftIndices.size, vBuilder.build()))
    }
    leftCol is LongColumn && rightCol is LongColumn -> {
        val data = LongArray(leftIndices.size)
        val vBuilder = ValidityBitmapBuilder(leftIndices.size)
        for (i in leftIndices.indices) {
            val lIdx = leftIndices[i]
            val rIdx = rightIndices[i]
            if (lIdx != -1 && leftCol.isValid(lIdx)) {
                vBuilder.appendValid()
                data[i] = leftCol.getLong(lIdx)
            } else if (rIdx != -1 && rightCol.isValid(rIdx)) {
                vBuilder.appendValid()
                data[i] = rightCol.getLong(rIdx)
            } else {
                vBuilder.appendNull()
                data[i] = 0L
            }
        }
        LongColumn(newName, LongBuffer(data, 0, leftIndices.size, vBuilder.build()))
    }
    leftCol is DoubleColumn && rightCol is DoubleColumn -> {
        val data = DoubleArray(leftIndices.size)
        val vBuilder = ValidityBitmapBuilder(leftIndices.size)
        for (i in leftIndices.indices) {
            val lIdx = leftIndices[i]
            val rIdx = rightIndices[i]
            if (lIdx != -1 && leftCol.isValid(lIdx)) {
                vBuilder.appendValid()
                data[i] = leftCol.getDouble(lIdx)
            } else if (rIdx != -1 && rightCol.isValid(rIdx)) {
                vBuilder.appendValid()
                data[i] = rightCol.getDouble(rIdx)
            } else {
                vBuilder.appendNull()
                data[i] = 0.0
            }
        }
        DoubleColumn(newName, DoubleBuffer(data, 0, leftIndices.size, vBuilder.build()))
    }
    leftCol is StringColumn && rightCol is StringColumn -> {
        val builder = Utf8StringBufferBuilder(leftIndices.size)
        for (i in leftIndices.indices) {
            val lIdx = leftIndices[i]
            val rIdx = rightIndices[i]
            if (lIdx != -1 && leftCol.isValid(lIdx)) {
                val s = leftCol.getString(lIdx)
                if (s != null) builder.append(s) else builder.appendNull()
            } else if (rIdx != -1 && rightCol.isValid(rIdx)) {
                val s = rightCol.getString(rIdx)
                if (s != null) builder.append(s) else builder.appendNull()
            } else {
                builder.appendNull()
            }
        }
        StringColumn(newName, builder.build())
    }
    else -> {
        // Fallback for general types
        remapColumn(leftCol, leftIndices, newName)
    }
}
