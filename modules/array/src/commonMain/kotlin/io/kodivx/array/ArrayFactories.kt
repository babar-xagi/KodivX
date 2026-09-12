package io.kodivx.array

/**
 * Creates a 1D [DoubleVector] from the specified double values.
 *
 * Example:
 * ```kotlin
 * val x = array(1.0, 2.0, 3.0, 4.0)
 * println(x.mean())
 * ```
 */
fun array(vararg values: Double): DoubleVector = DoubleVector.of(*values)

/**
 * Creates a 1D [DoubleVector] from integer values.
 */
fun array(vararg values: Int): DoubleVector =
    DoubleVector(DoubleArray(values.size) { values[it].toDouble() })

/**
 * Creates a 1D [DoubleVector] populated with zeros of the given [size].
 */
fun zeros(size: Int): DoubleVector = DoubleVector.zeros(size)

/**
 * Creates a 1D [DoubleVector] populated with ones of the given [size].
 */
fun ones(size: Int): DoubleVector = DoubleVector.ones(size)

/**
 * Generates [num] evenly spaced values between [start] and [stop].
 */
fun linspace(start: Double, stop: Double, num: Int): DoubleVector =
    DoubleVector.linspace(start, stop, num)

/**
 * Creates a 2D [DoubleMatrix] with the specified [rows], [cols], and values.
 *
 * Example:
 * ```kotlin
 * val m = matrixOf(2, 2, 1.0, 2.0, 3.0, 4.0)
 * println(m.transpose())
 * ```
 */
fun matrixOf(rows: Int, cols: Int, vararg values: Double): DoubleMatrix =
    DoubleMatrix.of(rows, cols, *values)

/**
 * Creates an identity matrix of dimension [size] x [size].
 */
fun eye(size: Int): DoubleMatrix = DoubleMatrix.eye(size)

/**
 * Creates a 2D [DoubleMatrix] populated with zeros.
 */
fun zeros(rows: Int, cols: Int): DoubleMatrix = DoubleMatrix.zeros(rows, cols)

/**
 * Creates a 2D [DoubleMatrix] populated with ones.
 */
fun ones(rows: Int, cols: Int): DoubleMatrix = DoubleMatrix.ones(rows, cols)
