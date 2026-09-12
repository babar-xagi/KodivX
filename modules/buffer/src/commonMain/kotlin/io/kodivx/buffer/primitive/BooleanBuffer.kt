package io.kodivx.buffer.primitive

import io.kodivx.buffer.Buffer
import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.core.error.BufferOutOfBoundsException
import io.kodivx.core.type.DataType

/**
 * Memory-compact Boolean buffer storing values bit-packed into 64-bit words ([LongArray]),
 * achieving 1 bit per boolean value (8x smaller than standard boolean arrays).
 */
class BooleanBuffer(
    val words: LongArray,
    val offset: Int = 0,
    override val size: Int,
    override val validity: ValidityBitmap? = null
) : Buffer {
    override val type: DataType get() = DataType.Boolean

    init {
        require(offset >= 0) { "Offset must be non-negative: $offset" }
        require(size >= 0) { "Size must be non-negative: $size" }
        val requiredWords = (offset + size + 63) ushr 6
        require(words.size >= requiredWords) {
            "Words array length (${words.size}) is insufficient for ${offset + size} bits"
        }
        if (validity != null) {
            require(validity.size == size) {
                "Validity bitmap size (${validity.size}) must match buffer size ($size)"
            }
        }
    }

    /**
     * Returns the boolean value at the given logical [index].
     */
    fun getBoolean(index: Int): Boolean {
        if (index !in 0 until size) throw BufferOutOfBoundsException(index, size)
        val bitPos = offset + index
        val wordIndex = bitPos ushr 6
        val bitInWord = bitPos and 63
        return (words[wordIndex] and (1L shl bitInWord)) != 0L
    }

    override fun getBoxed(index: Int): Boolean? {
        if (isNull(index)) return null
        return getBoolean(index)
    }

    override fun slice(offset: Int, length: Int): BooleanBuffer {
        if (offset < 0 || length < 0 || offset + length > size) {
            throw BufferOutOfBoundsException(offset + length, size)
        }
        val slicedValidity = validity?.slice(offset, length)
        return BooleanBuffer(words, this.offset + offset, length, slicedValidity)
    }

    companion object {
        fun of(vararg values: Boolean): BooleanBuffer {
            val wordCount = (values.size + 63) ushr 6
            val words = LongArray(wordCount)
            values.forEachIndexed { index, value ->
                if (value) {
                    val wordIdx = index ushr 6
                    val bitIdx = index and 63
                    words[wordIdx] = words[wordIdx] or (1L shl bitIdx)
                }
            }
            return BooleanBuffer(words, 0, values.size)
        }

        fun ofNullable(values: List<Boolean?>): BooleanBuffer {
            val wordCount = (values.size + 63) ushr 6
            val words = LongArray(wordCount)
            val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(values.size)

            values.forEachIndexed { index, value ->
                if (value != null) {
                    vBuilder.appendValid()
                    if (value) {
                        val wordIdx = index ushr 6
                        val bitIdx = index and 63
                        words[wordIdx] = words[wordIdx] or (1L shl bitIdx)
                    }
                } else {
                    vBuilder.appendNull()
                }
            }
            return BooleanBuffer(words, 0, values.size, vBuilder.build())
        }
    }
}
