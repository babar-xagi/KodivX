package io.kodivx.array

import io.kodivx.core.error.BufferOutOfBoundsException
import kotlin.math.sqrt

/**
 * 1D numerical vector backed by a contiguous or strided [DoubleArray].
 * Supports vectorized element-wise arithmetic, reductions, zero-copy slicing, and matrix reshaping.
 */
class DoubleVector(
    val data: DoubleArray,
    val offset: Int = 0,
    val size: Int = data.size - offset,
    val stride: Int = 1
) {
    init {
        require(size >= 0) { "Size must be non-negative: $size" }
        require(stride != 0) { "Stride cannot be 0" }
        if (size > 0) {
            val maxIndex = offset + (size - 1) * stride
            require(offset >= 0 && maxIndex < data.size) {
                "Offset $offset with size $size and stride $stride exceeds data array size ${data.size}"
            }
        }
    }

    val shape: Shape = Shape(size)

    val isContiguous: Boolean get() = stride == 1

    operator fun get(index: Int): Double {
        if (index !in 0 until size) throw BufferOutOfBoundsException(index, size)
        return data[offset + index * stride]
    }

    /**
     * Creates a zero-copy strided slice of this vector.
     */
    fun slice(start: Int, endExclusive: Int, step: Int = 1): DoubleVector {
        require(start in 0..size) { "Start index out of bounds: $start" }
        require(endExclusive in 0..size) { "End index out of bounds: $endExclusive" }
        require(step > 0) { "Step must be positive: $step" }
        if (start >= endExclusive) return DoubleVector(data, offset, 0, stride)

        val sliceLength = (endExclusive - start + step - 1) / step
        return DoubleVector(
            data = data,
            offset = offset + start * stride,
            size = sliceLength,
            stride = stride * step
        )
    }

    /**
     * In-place iteration without object allocations.
     */
    inline fun forEach(action: (Double) -> Unit) {
        if (isContiguous) {
            val end = offset + size
            for (i in offset until end) {
                action(data[i])
            }
        } else {
            var idx = offset
            for (i in 0 until size) {
                action(data[idx])
                idx += stride
            }
        }
    }

    // --- Vector Arithmetic (Element-wise) ---

    operator fun plus(other: DoubleVector): DoubleVector {
        require(size == other.size) { "Vector size mismatch: $size vs ${other.size}" }
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] + other[i]
        }
        return DoubleVector(out)
    }

    operator fun plus(scalar: Double): DoubleVector {
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] + scalar
        }
        return DoubleVector(out)
    }

    operator fun minus(other: DoubleVector): DoubleVector {
        require(size == other.size) { "Vector size mismatch: $size vs ${other.size}" }
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] - other[i]
        }
        return DoubleVector(out)
    }

    operator fun minus(scalar: Double): DoubleVector {
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] - scalar
        }
        return DoubleVector(out)
    }

    operator fun times(other: DoubleVector): DoubleVector {
        require(size == other.size) { "Vector size mismatch: $size vs ${other.size}" }
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] * other[i]
        }
        return DoubleVector(out)
    }

    operator fun times(scalar: Double): DoubleVector {
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] * scalar
        }
        return DoubleVector(out)
    }

    operator fun div(other: DoubleVector): DoubleVector {
        require(size == other.size) { "Vector size mismatch: $size vs ${other.size}" }
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] / other[i]
        }
        return DoubleVector(out)
    }

    operator fun div(scalar: Double): DoubleVector {
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = this[i] / scalar
        }
        return DoubleVector(out)
    }

    operator fun unaryMinus(): DoubleVector {
        val out = DoubleArray(size)
        for (i in 0 until size) {
            out[i] = -this[i]
        }
        return DoubleVector(out)
    }

    // --- Reductions ---

    fun sum(): Double {
        var s = 0.0
        forEach { s += it }
        return s
    }

    fun mean(): Double {
        require(size > 0) { "Cannot compute mean of empty vector" }
        return sum() / size
    }

    fun min(): Double {
        require(size > 0) { "Cannot compute min of empty vector" }
        var m = this[0]
        for (i in 1 until size) {
            val v = this[i]
            if (v < m) m = v
        }
        return m
    }

    fun max(): Double {
        require(size > 0) { "Cannot compute max of empty vector" }
        var m = this[0]
        for (i in 1 until size) {
            val v = this[i]
            if (v > m) m = v
        }
        return m
    }

    fun variance(isSample: Boolean = true): Double {
        require(size > (if (isSample) 1 else 0)) {
            "Insufficient elements for variance (size: $size, sample: $isSample)"
        }
        val m = mean()
        var sumSq = 0.0
        forEach { v ->
            val diff = v - m
            sumSq += diff * diff
        }
        val denom = if (isSample) size - 1 else size
        return sumSq / denom
    }

    fun stdDev(isSample: Boolean = true): Double = sqrt(variance(isSample))

    // --- Linear Algebra ---

    /**
     * Computes the vector dot product: sum(a[i] * b[i]).
     */
    infix fun dot(other: DoubleVector): Double {
        require(size == other.size) { "Vector sizes must match for dot product: $size vs ${other.size}" }
        var acc = 0.0
        for (i in 0 until size) {
            acc += this[i] * other[i]
        }
        return acc
    }

    /**
     * Euclidean L2 norm of the vector: sqrt(sum(x^2)).
     */
    fun norm(): Double = sqrt(dot(this))

    /**
     * Reshapes this 1D vector into a 2D [DoubleMatrix] with specified [rows] and [cols].
     */
    fun reshape(rows: Int, cols: Int): DoubleMatrix {
        require(rows * cols == size) {
            "Cannot reshape vector of size $size into shape ($rows, $cols)"
        }
        // If contiguous, share underlying array; otherwise materialize contiguous data
        val arrayData = if (isContiguous && offset == 0) data else toDoubleArray()
        return DoubleMatrix(
            data = arrayData,
            rows = rows,
            cols = cols,
            rowStride = cols,
            colStride = 1
        )
    }

    fun toDoubleArray(): DoubleArray {
        val arr = DoubleArray(size)
        for (i in 0 until size) arr[i] = this[i]
        return arr
    }

    override fun toString(): String = buildString {
        append("array([")
        val limit = 10
        val itemsToShow = kotlin.math.min(size, limit)
        for (i in 0 until itemsToShow) {
            if (i > 0) append(", ")
            append(this@DoubleVector[i])
        }
        if (size > limit) append(", ... (${size - limit} more)")
        append("])")
    }

    companion object {
        fun of(vararg values: Double): DoubleVector = DoubleVector(values)
        fun fromArray(values: DoubleArray): DoubleVector = DoubleVector(values)
        fun zeros(size: Int): DoubleVector = DoubleVector(DoubleArray(size))
        fun ones(size: Int): DoubleVector = DoubleVector(DoubleArray(size) { 1.0 })

        fun linspace(start: Double, stop: Double, num: Int): DoubleVector {
            require(num >= 2) { "Number of samples must be at least 2: $num" }
            val step = (stop - start) / (num - 1)
            val data = DoubleArray(num) { i -> start + i * step }
            return DoubleVector(data)
        }
    }
}
