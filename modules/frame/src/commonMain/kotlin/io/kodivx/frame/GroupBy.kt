package io.kodivx.frame

import io.kodivx.buffer.builder.DoubleBufferBuilder
import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.primitive.IntBuffer
import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.core.error.ColumnNotFoundException
import kotlin.math.sqrt

/**
 * Compound key representing group identity across one or more columns.
 */
class GroupKey(val values: Array<Any?>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupKey) return false
        return values.contentEquals(other.values)
    }

    override fun hashCode(): Int = values.contentHashCode()

    override fun toString(): String = values.contentToString()
}

/**
 * Represents a DataFrame partitioned into groups by one or more key columns.
 */
class GroupedDataFrame(
    val df: DataFrame,
    val groupKeys: List<String>
) {
    init {
        require(groupKeys.isNotEmpty()) { "At least one grouping column must be specified" }
        for (key in groupKeys) {
            if (df.schema.indexOf(key) == -1) {
                throw ColumnNotFoundException(key, df.columnNames)
            }
        }
    }

    // Partition rows into groups preserving first-seen insertion order
    private val groups: Map<GroupKey, List<Int>> by lazy {
        val keyCols = groupKeys.map { df[it] }
        val map = LinkedHashMap<GroupKey, MutableList<Int>>()
        for (r in 0 until df.rowCount) {
            val keyArr = Array<Any?>(keyCols.size) { colIdx -> keyCols[colIdx][r] }
            val key = GroupKey(keyArr)
            map.getOrPut(key) { mutableListOf() }.add(r)
        }
        map
    }

    val groupCount: Int get() = groups.size

    fun count(): DataFrame = agg(Aggregation.Count())

    fun sum(vararg columns: String): DataFrame =
        agg(columns.map { Aggregation.Sum(it) })

    fun mean(vararg columns: String): DataFrame =
        agg(columns.map { Aggregation.Mean(it) })

    fun min(vararg columns: String): DataFrame =
        agg(columns.map { Aggregation.Min(it) })

    fun max(vararg columns: String): DataFrame =
        agg(columns.map { Aggregation.Max(it) })

    fun agg(vararg aggregations: Aggregation): DataFrame = agg(aggregations.toList())

    fun agg(aggregations: List<Aggregation>): DataFrame {
        require(aggregations.isNotEmpty()) { "At least one aggregation must be provided" }

        if (groups.isEmpty() || df.rowCount == 0) {
            val emptyKeyCols = groupKeys.map { df[it].slice(0, 0) }
            val emptyAggCols = aggregations.map { agg ->
                when (agg) {
                    is Aggregation.Count -> IntColumn(agg.outputName, IntBuffer(IntArray(0)))
                    else -> DoubleColumn(agg.outputName, DoubleBuffer(DoubleArray(0)))
                }
            }
            return dataFrameOf(emptyKeyCols + emptyAggCols)
        }

        // Build key columns by taking the first row of each group via SelectionVector
        val numGroups = groups.size
        val repIndices = IntArray(numGroups)
        var gIdx = 0
        for ((_, indices) in groups) {
            repIndices[gIdx++] = indices[0]
        }
        val keySelection = SelectionVector(repIndices)
        val outKeyColumns = groupKeys.map { keyName ->
            df[keyName].filterWithSelection(keySelection)
        }

        // Evaluate each aggregation over all groups
        val outAggColumns = aggregations.map { agg ->
            when (agg) {
                is Aggregation.Count -> computeCount(agg, numGroups)
                is Aggregation.Sum -> computeSum(agg, numGroups)
                is Aggregation.Mean -> computeMean(agg, numGroups)
                is Aggregation.Min -> computeMin(agg, numGroups)
                is Aggregation.Max -> computeMax(agg, numGroups)
                is Aggregation.Variance -> computeVariance(agg, numGroups)
                is Aggregation.StdDev -> computeStdDev(agg, numGroups)
            }
        }

        return dataFrameOf(outKeyColumns + outAggColumns)
    }

    private fun computeCount(agg: Aggregation.Count, numGroups: Int): Column<Int> {
        val outCounts = IntArray(numGroups)
        var g = 0
        if (agg.column == null) {
            for ((_, indices) in groups) {
                outCounts[g++] = indices.size
            }
        } else {
            val targetCol = df[agg.column]
            for ((_, indices) in groups) {
                var c = 0
                for (i in 0 until indices.size) {
                    if (targetCol.isValid(indices[i])) c++
                }
                outCounts[g++] = c
            }
        }
        return IntColumn(agg.outputName, IntBuffer(outCounts))
    }

    private fun computeSum(agg: Aggregation.Sum, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var sum = 0.0
            var validCount = 0
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        sum += v.toDouble()
                        validCount++
                    }
                }
            }
            if (validCount > 0) {
                builder.append(sum)
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }

    private fun computeMean(agg: Aggregation.Mean, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var sum = 0.0
            var validCount = 0
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        sum += v.toDouble()
                        validCount++
                    }
                }
            }
            if (validCount > 0) {
                builder.append(sum / validCount)
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }

    private fun computeMin(agg: Aggregation.Min, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var minVal: Double? = null
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        val d = v.toDouble()
                        if (minVal == null || d < minVal) minVal = d
                    }
                }
            }
            if (minVal != null) {
                builder.append(minVal)
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }

    private fun computeMax(agg: Aggregation.Max, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var maxVal: Double? = null
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        val d = v.toDouble()
                        if (maxVal == null || d > maxVal) maxVal = d
                    }
                }
            }
            if (maxVal != null) {
                builder.append(maxVal)
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }

    private fun computeVariance(agg: Aggregation.Variance, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var sum = 0.0
            var validCount = 0
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        sum += v.toDouble()
                        validCount++
                    }
                }
            }
            if (validCount > 1) {
                val mean = sum / validCount
                var sumSqDiff = 0.0
                for (i in 0 until indices.size) {
                    val rowIdx = indices[i]
                    if (targetCol.isValid(rowIdx)) {
                        val v = targetCol[rowIdx]
                        if (v is Number) {
                            val diff = v.toDouble() - mean
                            sumSqDiff += diff * diff
                        }
                    }
                }
                builder.append(sumSqDiff / (validCount - 1))
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }

    private fun computeStdDev(agg: Aggregation.StdDev, numGroups: Int): Column<Double> {
        val targetCol = df[agg.column]
        val builder = DoubleBufferBuilder(numGroups)
        for ((_, indices) in groups) {
            var sum = 0.0
            var validCount = 0
            for (i in 0 until indices.size) {
                val rowIdx = indices[i]
                if (targetCol.isValid(rowIdx)) {
                    val v = targetCol[rowIdx]
                    if (v is Number) {
                        sum += v.toDouble()
                        validCount++
                    }
                }
            }
            if (validCount > 1) {
                val mean = sum / validCount
                var sumSqDiff = 0.0
                for (i in 0 until indices.size) {
                    val rowIdx = indices[i]
                    if (targetCol.isValid(rowIdx)) {
                        val v = targetCol[rowIdx]
                        if (v is Number) {
                            val diff = v.toDouble() - mean
                            sumSqDiff += diff * diff
                        }
                    }
                }
                val variance = sumSqDiff / (validCount - 1)
                builder.append(sqrt(variance))
            } else {
                builder.appendNull()
            }
        }
        return DoubleColumn(agg.outputName, builder.build())
    }
}

fun DataFrame.groupBy(vararg keys: String): GroupedDataFrame = GroupedDataFrame(this, keys.toList())

fun DataFrame.groupBy(keys: List<String>): GroupedDataFrame = GroupedDataFrame(this, keys)
