package io.kodivx.array

import io.kodivx.core.error.BufferOutOfBoundsException

/**
 * 2D numerical matrix backed by a strided [DoubleArray].
 * Supports zero-copy transpositions, zero-copy row/column vector slicing,
 * broadcasting arithmetic, matrix multiplication, and axis reductions.
 */
class DoubleMatrix(
    val data: DoubleArray,
    val rows: Int,
    val cols: Int,
    val offset: Int = 0,
    val rowStride: Int = cols,
    val colStride: Int = 1
) {
    init {
        require(rows >= 0) { "Rows must be non-negative: $rows" }
        require(cols >= 0) { "Cols must be non-negative: $cols" }
        if (rows > 0 && cols > 0) {
            val maxIndex = offset + (rows - 1) * rowStride + (cols - 1) * colStride
            require(offset >= 0 && maxIndex < data.size) {
                "Matrix indexing parameters exceed backing data size ${data.size}"
            }
        }
    }

    val shape: Shape = Shape(rows, cols)
    val size: Int get() = rows * cols

    operator fun get(row: Int, col: Int): Double {
        if (row !in 0 until rows) throw BufferOutOfBoundsException(row, rows)
        if (col !in 0 until cols) throw BufferOutOfBoundsException(col, cols)
        return data[offset + row * rowStride + col * colStride]
    }

    /**
     * Returns a zero-copy [DoubleVector] view of the specified row [r].
     */
    fun row(r: Int): DoubleVector {
        if (r !in 0 until rows) throw BufferOutOfBoundsException(r, rows)
        return DoubleVector(
            data = data,
            offset = offset + r * rowStride,
            size = cols,
            stride = colStride
        )
    }

    /**
     * Returns a zero-copy [DoubleVector] view of the specified column [c].
     */
    fun col(c: Int): DoubleVector {
        if (c !in 0 until cols) throw BufferOutOfBoundsException(c, cols)
        return DoubleVector(
            data = data,
            offset = offset + c * colStride,
            size = rows,
            stride = rowStride
        )
    }

    /**
     * O(1) Zero-Copy Transpose by swapping dimensions and strides.
     */
    fun transpose(): DoubleMatrix = DoubleMatrix(
        data = data,
        rows = cols,
        cols = rows,
        offset = offset,
        rowStride = colStride,
        colStride = rowStride
    )

    // --- Arithmetic & Broadcasting ---

    operator fun plus(other: DoubleMatrix): DoubleMatrix {
        require(rows == other.rows && cols == other.cols) {
            "Matrix shape mismatch: ($rows, $cols) vs (${other.rows}, ${other.cols})"
        }
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] + other[r, c]
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    operator fun plus(scalar: Double): DoubleMatrix {
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] + scalar
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    /**
     * Broadcast addition: adds a vector of length [cols] to every row of the matrix.
     */
    operator fun plus(vector: DoubleVector): DoubleMatrix {
        require(cols == vector.size) {
            "Broadcast vector size (${vector.size}) must match matrix column count ($cols)"
        }
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] + vector[c]
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    operator fun minus(other: DoubleMatrix): DoubleMatrix {
        require(rows == other.rows && cols == other.cols) {
            "Matrix shape mismatch: ($rows, $cols) vs (${other.rows}, ${other.cols})"
        }
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] - other[r, c]
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    operator fun minus(scalar: Double): DoubleMatrix {
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] - scalar
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    operator fun times(scalar: Double): DoubleMatrix {
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] * scalar
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    /**
     * Element-wise Hadamard product.
     */
    operator fun times(other: DoubleMatrix): DoubleMatrix {
        require(rows == other.rows && cols == other.cols) {
            "Matrix shape mismatch for element-wise multiplication: ($rows, $cols) vs (${other.rows}, ${other.cols})"
        }
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] * other[r, c]
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    operator fun div(scalar: Double): DoubleMatrix {
        val out = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                out[idx++] = this[r, c] / scalar
            }
        }
        return DoubleMatrix(out, rows, cols)
    }

    // --- Linear Algebra & Matrix Multiplication ---

    /**
     * Matrix multiplication: (M x K) * (K x N) -> (M x N).
     */
    infix fun matmul(other: DoubleMatrix): DoubleMatrix {
        require(cols == other.rows) {
            "Matrix dimensions incompatible for multiplication: ($rows, $cols) * (${other.rows}, ${other.cols})"
        }
        val out = DoubleArray(rows * other.cols)
        for (i in 0 until rows) {
            for (k in 0 until cols) {
                val aVal = this[i, k]
                val outRowOffset = i * other.cols
                for (j in 0 until other.cols) {
                    out[outRowOffset + j] += aVal * other[k, j]
                }
            }
        }
        return DoubleMatrix(out, rows, other.cols)
    }

    /**
     * Matrix-Vector multiplication: (M x K) * (K,) -> (M,).
     */
    infix fun dot(vector: DoubleVector): DoubleVector {
        require(cols == vector.size) {
            "Matrix cols ($cols) must match vector size (${vector.size})"
        }
        val out = DoubleArray(rows)
        for (r in 0 until rows) {
            out[r] = this.row(r) dot vector
        }
        return DoubleVector(out)
    }

    // --- Reductions ---

    fun sum(): Double {
        var s = 0.0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                s += this[r, c]
            }
        }
        return s
    }

    fun mean(): Double {
        require(size > 0) { "Cannot compute mean of empty matrix" }
        return sum() / size
    }

    fun min(): Double {
        require(size > 0) { "Cannot compute min of empty matrix" }
        var m = this[0, 0]
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val v = this[r, c]
                if (v < m) m = v
            }
        }
        return m
    }

    fun max(): Double {
        require(size > 0) { "Cannot compute max of empty matrix" }
        var m = this[0, 0]
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val v = this[r, c]
                if (v > m) m = v
            }
        }
        return m
    }

    /**
     * Sum across the specified axis:
     * - axis = 0: sum across rows, returning a vector of column sums (length [cols])
     * - axis = 1: sum across columns, returning a vector of row sums (length [rows])
     */
    fun sum(axis: Int): DoubleVector {
        require(axis == 0 || axis == 1) { "Invalid axis: $axis (must be 0 or 1)" }
        return if (axis == 0) {
            val out = DoubleArray(cols)
            for (c in 0 until cols) {
                out[c] = col(c).sum()
            }
            DoubleVector(out)
        } else {
            val out = DoubleArray(rows)
            for (r in 0 until rows) {
                out[r] = row(r).sum()
            }
            DoubleVector(out)
        }
    }

    /**
     * Mean across the specified axis.
     */
    fun mean(axis: Int): DoubleVector {
        require(axis == 0 || axis == 1) { "Invalid axis: $axis (must be 0 or 1)" }
        return if (axis == 0) {
            val out = DoubleArray(cols)
            for (c in 0 until cols) {
                out[c] = col(c).mean()
            }
            DoubleVector(out)
        } else {
            val out = DoubleArray(rows)
            for (r in 0 until rows) {
                out[r] = row(r).mean()
            }
            DoubleVector(out)
        }
    }

    fun toDoubleArray(): DoubleArray {
        val arr = DoubleArray(size)
        var idx = 0
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                arr[idx++] = this[r, c]
            }
        }
        return arr
    }

    override fun toString(): String = buildString {
        append("matrix([\n")
        val maxRows = 6
        val rowsToShow = kotlin.math.min(rows, maxRows)
        for (r in 0 until rowsToShow) {
            append("  ")
            append(row(r).toString())
            if (r < rows - 1) append(",\n")
        }
        if (rows > maxRows) append("\n  ... (${rows - maxRows} more rows)")
        append("\n])")
    }

    companion object {
        fun of(rows: Int, cols: Int, vararg values: Double): DoubleMatrix {
            require(values.size == rows * cols) {
                "Values length (${values.size}) must equal rows * cols ($rows * $cols = ${rows * cols})"
            }
            return DoubleMatrix(values, rows, cols)
        }

        fun zeros(rows: Int, cols: Int): DoubleMatrix =
            DoubleMatrix(DoubleArray(rows * cols), rows, cols)

        fun ones(rows: Int, cols: Int): DoubleMatrix =
            DoubleMatrix(DoubleArray(rows * cols) { 1.0 }, rows, cols)

        fun eye(size: Int): DoubleMatrix {
            val data = DoubleArray(size * size)
            for (i in 0 until size) {
                data[i * size + i] = 1.0
            }
            return DoubleMatrix(data, size, size)
        }
    }
}
