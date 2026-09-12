package io.kodivx.buffer.primitive

import io.kodivx.buffer.Buffer
import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.core.error.BufferOutOfBoundsException
import io.kodivx.core.type.DataType

/**
 * 64-bit signed integer buffer backed by a contiguous [LongArray].
 */
class LongBuffer(
    val data: LongArray,
    val offset: Int = 0,
    override val size: Int = data.size - offset,
    override val validity: ValidityBitmap? = null
) : Buffer {
    override val type: DataType get() = DataType.Int64

    init {
        require(offset >= 0) { "Offset must be non-negative: $offset" }
        require(size >= 0) { "Size must be non-negative: $size" }
        require(offset + size <= data.size) {
            "Offset ($offset) + size ($size) exceeds backing array length (${data.size})"
        }
        if (validity != null) {
            require(validity.size == size) {
                "Validity bitmap size (${validity.size}) must match buffer size ($size)"
            }
        }
    }

    fun getLong(index: Int): Long {
        if (index !in 0 until size) throw BufferOutOfBoundsException(index, size)
        return data[offset + index]
    }

    override fun getBoxed(index: Int): Long? {
        if (isNull(index)) return null
        return getLong(index)
    }

    override fun slice(offset: Int, length: Int): LongBuffer {
        if (offset < 0 || length < 0 || offset + length > size) {
            throw BufferOutOfBoundsException(offset + length, size)
        }
        val slicedValidity = validity?.slice(offset, length)
        return LongBuffer(data, this.offset + offset, length, slicedValidity)
    }

    inline fun forEachLong(action: (Long) -> Unit) {
        val start = offset
        val end = start + size
        for (i in start until end) {
            action(data[i])
        }
    }

    companion object {
        fun of(vararg values: Long): LongBuffer = LongBuffer(values)
        fun ofNullable(values: List<Long?>): LongBuffer {
            val arr = LongArray(values.size)
            val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(values.size)
            values.forEachIndexed { i, v ->
                if (v != null) {
                    arr[i] = v
                    vBuilder.appendValid()
                } else {
                    vBuilder.appendNull()
                }
            }
            return LongBuffer(arr, 0, values.size, vBuilder.build())
        }
    }
}
