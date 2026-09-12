package io.kodivx.buffer

import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.core.type.DataType

/**
 * Base abstraction for all contiguous columnar data buffers in KodivX.
 *
 * Buffers represent typed, physical column storage backed by specialized arrays
 * with optional validity bitmaps for null tracking.
 */
interface Buffer {
    val type: DataType
    val size: Int
    val validity: ValidityBitmap?

    val isNullable: Boolean get() = validity != null

    val nullCount: Int
        get() = if (validity != null) size - validity!!.countValid() else 0

    fun isNull(index: Int): Boolean = validity?.isNull(index) ?: false
    fun isValid(index: Int): Boolean = validity?.isValid(index) ?: true

    /**
     * Obtains the boxed value at the given logical [index], primarily for debug and dynamic code.
     * High-performance execution loops should use specialized unboxed getters (`getInt`, `getDouble`, etc.).
     */
    fun getBoxed(index: Int): Any?

    /**
     * Returns a zero-copy slice view over this buffer.
     */
    fun slice(offset: Int, length: Int): Buffer
}
