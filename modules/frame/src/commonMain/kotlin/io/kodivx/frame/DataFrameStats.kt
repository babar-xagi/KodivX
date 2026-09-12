package io.kodivx.frame

import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.string.Utf8StringBuffer
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Computes sample variance for a numeric column ($s^2 = \frac{1}{N-1} \sum (x_i - \bar{x})^2$).
 * Returns [Double.NaN] if there are fewer than 2 valid numeric values.
 */
fun DataFrame.variance(columnName: String): Double {
    val col = this[columnName]
    var sum = 0.0
    var count = 0
    for (i in 0 until rowCount) {
        if (col.isValid(i)) {
            val v = col[i]
            if (v is Number) {
                sum += v.toDouble()
                count++
            }
        }
    }
    if (count <= 1) return Double.NaN

    val mean = sum / count
    var sumSqDiff = 0.0
    for (i in 0 until rowCount) {
        if (col.isValid(i)) {
            val v = col[i]
            if (v is Number) {
                val diff = v.toDouble() - mean
                sumSqDiff += diff * diff
            }
        }
    }
    return sumSqDiff / (count - 1)
}

/**
 * Computes sample standard deviation for a numeric column ($s = \sqrt{\text{variance}}$).
 * Returns [Double.NaN] if there are fewer than 2 valid numeric values.
 */
fun DataFrame.stdDev(columnName: String): Double {
    val v = variance(columnName)
    return if (v.isNaN() || v < 0.0) Double.NaN else sqrt(v)
}

/**
 * Computes the [q]-th quantile ($0.0 \le q \le 1.0$) for a numeric column using linear interpolation.
 */
fun DataFrame.quantile(columnName: String, q: Double): Double {
    require(q in 0.0..1.0) { "Quantile must be between 0.0 and 1.0, got: $q" }
    val col = this[columnName]
    val values = DoubleArrayList()
    for (i in 0 until rowCount) {
        if (col.isValid(i)) {
            val v = col[i]
            if (v is Number) {
                values.add(v.toDouble())
            }
        }
    }
    require(values.size > 0) { "Cannot compute quantile on empty or all-null column '$columnName'" }

    val sorted = values.toDoubleArray()
    sorted.sort()

    val idx = q * (sorted.size - 1)
    val low = idx.toInt()
    val high = min(low + 1, sorted.size - 1)
    val weight = idx - low
    return sorted[low] * (1.0 - weight) + sorted[high] * weight
}

/**
 * Computes the median (50th percentile) for a numeric column.
 */
fun DataFrame.median(columnName: String): Double = quantile(columnName, 0.5)

/**
 * Computes sample covariance between two numeric columns.
 * Rows with null or non-numeric values in either column are omitted.
 * Returns [Double.NaN] if fewer than 2 complete pairs are found.
 */
fun DataFrame.covariance(colA: String, colB: String): Double {
    val aCol = this[colA]
    val bCol = this[colB]

    var sumA = 0.0
    var sumB = 0.0
    var pairCount = 0

    for (i in 0 until rowCount) {
        if (aCol.isValid(i) && bCol.isValid(i)) {
            val aVal = aCol[i]
            val bVal = bCol[i]
            if (aVal is Number && bVal is Number) {
                sumA += aVal.toDouble()
                sumB += bVal.toDouble()
                pairCount++
            }
        }
    }
    if (pairCount <= 1) return Double.NaN

    val meanA = sumA / pairCount
    val meanB = sumB / pairCount

    var covSum = 0.0
    for (i in 0 until rowCount) {
        if (aCol.isValid(i) && bCol.isValid(i)) {
            val aVal = aCol[i]
            val bVal = bCol[i]
            if (aVal is Number && bVal is Number) {
                covSum += (aVal.toDouble() - meanA) * (bVal.toDouble() - meanB)
            }
        }
    }
    return covSum / (pairCount - 1)
}

/**
 * Computes Pearson correlation coefficient between two numeric columns.
 * Returns [Double.NaN] if standard deviation of either column is 0 or undefined.
 */
fun DataFrame.correlation(colA: String, colB: String): Double {
    val aCol = this[colA]
    val bCol = this[colB]

    var sumA = 0.0
    var sumB = 0.0
    var pairCount = 0

    for (i in 0 until rowCount) {
        if (aCol.isValid(i) && bCol.isValid(i)) {
            val aVal = aCol[i]
            val bVal = bCol[i]
            if (aVal is Number && bVal is Number) {
                sumA += aVal.toDouble()
                sumB += bVal.toDouble()
                pairCount++
            }
        }
    }
    if (pairCount <= 1) return Double.NaN

    val meanA = sumA / pairCount
    val meanB = sumB / pairCount

    var covSum = 0.0
    var sqDiffA = 0.0
    var sqDiffB = 0.0

    for (i in 0 until rowCount) {
        if (aCol.isValid(i) && bCol.isValid(i)) {
            val aVal = aCol[i]
            val bVal = bCol[i]
            if (aVal is Number && bVal is Number) {
                val diffA = aVal.toDouble() - meanA
                val diffB = bVal.toDouble() - meanB
                covSum += diffA * diffB
                sqDiffA += diffA * diffA
                sqDiffB += diffB * diffB
            }
        }
    }

    val denom = sqrt(sqDiffA * sqDiffB)
    if (denom == 0.0 || denom.isNaN()) return Double.NaN

    val r = covSum / denom
    return r.coerceIn(-1.0, 1.0)
}

/**
 * Computes summary statistics (count, mean, std, min, 25%, 50%, 75%, max) for all numeric columns.
 */
fun DataFrame.describe(): DataFrame {
    val numericCols = columns.filter {
        it is IntColumn || it is LongColumn || it is DoubleColumn || it is FloatColumn
    }

    val statLabels = listOf("count", "mean", "std", "min", "25%", "50%", "75%", "max")
    val statCol = StringColumn("statistic", Utf8StringBuffer.ofList(statLabels))

    if (numericCols.isEmpty()) {
        val emptyCols = listOf<Column<*>>(statCol)
        return dataFrameOf(emptyCols)
    }

    val outCols = mutableListOf<Column<*>>(statCol)

    for (col in numericCols) {
        val values = DoubleArrayList()
        var sum = 0.0
        var minVal: Double? = null
        var maxVal: Double? = null

        for (i in 0 until rowCount) {
            if (col.isValid(i)) {
                val v = col[i]
                if (v is Number) {
                    val d = v.toDouble()
                    values.add(d)
                    sum += d
                    if (minVal == null || d < minVal) minVal = d
                    if (maxVal == null || d > maxVal) maxVal = d
                }
            }
        }

        val count = values.size.toDouble()
        if (count == 0.0) {
            val naData = DoubleArray(8) { Double.NaN }
            naData[0] = 0.0
            outCols.add(DoubleColumn(col.name, DoubleBuffer(naData)))
            continue
        }

        val mean = sum / count

        val std = if (count > 1.0) {
            val meanVal = mean
            var sumSq = 0.0
            for (i in 0 until values.size) {
                val diff = values[i] - meanVal
                sumSq += diff * diff
            }
            sqrt(sumSq / (count - 1.0))
        } else {
            Double.NaN
        }

        val sorted = values.toDoubleArray()
        sorted.sort()

        fun calcQuantile(q: Double): Double {
            val idx = q * (sorted.size - 1)
            val low = idx.toInt()
            val high = min(low + 1, sorted.size - 1)
            val weight = idx - low
            return sorted[low] * (1.0 - weight) + sorted[high] * weight
        }

        val q25 = calcQuantile(0.25)
        val q50 = calcQuantile(0.50)
        val q75 = calcQuantile(0.75)

        val stats = doubleArrayOf(
            count,
            mean,
            std,
            minVal ?: Double.NaN,
            q25,
            q50,
            q75,
            maxVal ?: Double.NaN
        )

        outCols.add(DoubleColumn(col.name, DoubleBuffer(stats)))
    }

    return dataFrameOf(outCols)
}

/**
 * Minimal primitive double list helper avoiding boxing in quantile and stats computation.
 */
internal class DoubleArrayList(initialCapacity: Int = 16) {
    var data = DoubleArray(kotlin.math.max(4, initialCapacity))
        private set
    var size: Int = 0
        private set

    fun add(element: Double) {
        if (size >= data.size) {
            val next = DoubleArray(data.size * 2)
            data.copyInto(next)
            data = next
        }
        data[size++] = element
    }

    operator fun get(index: Int): Double = data[index]

    fun toDoubleArray(): DoubleArray {
        val res = DoubleArray(size)
        data.copyInto(res, endIndex = size)
        return res
    }
}
