package io.kodivx.buffer.string

import io.kodivx.buffer.Buffer
import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.core.error.BufferOutOfBoundsException
import io.kodivx.core.type.DataType

/**
 * Arrow-aligned UTF-8 string buffer storing variable-length text as a single contiguous
 * [ByteArray] alongside an [IntArray] of element offsets and an optional [ValidityBitmap].
 *
 * Avoids creating individual heap objects for each text cell.
 */
class Utf8StringBuffer(
    val byteData: ByteArray,
    val offsets: IntArray,
    val offsetRow: Int = 0,
    override val size: Int = offsets.size - 1 - offsetRow,
    override val validity: ValidityBitmap? = null
) : Buffer {
    override val type: DataType get() = DataType.Utf8

    init {
        require(offsetRow >= 0) { "Row offset must be non-negative: $offsetRow" }
        require(size >= 0) { "Size must be non-negative: $size" }
        require(offsets.size >= offsetRow + size + 1) {
            "Offsets array length (${offsets.size}) insufficient for rowCount ($size)"
        }
        if (validity != null) {
            require(validity.size == size) {
                "Validity bitmap size (${validity.size}) must match buffer size ($size)"
            }
        }
    }

    /**
     * Gets the byte length of the string at [index].
     */
    fun getByteLength(index: Int): Int {
        if (index !in 0 until size) throw BufferOutOfBoundsException(index, size)
        val row = offsetRow + index
        return offsets[row + 1] - offsets[row]
    }

    /**
     * Decodes the string at [index] into a Kotlin [String].
     * Returns null if the element is marked null in the validity bitmap.
     */
    fun getString(index: Int): String? {
        if (isNull(index)) return null
        val row = offsetRow + index
        val start = offsets[row]
        val end = offsets[row + 1]
        return byteData.decodeToString(start, end)
    }

    override fun getBoxed(index: Int): String? = getString(index)

    /**
     * Compares the UTF-8 bytes at [index] directly against a target UTF-8 [ByteArray]
     * with zero object allocations.
     */
    fun equalsUtf8(index: Int, target: ByteArray): Boolean {
        if (isNull(index)) return false
        val row = offsetRow + index
        val start = offsets[row]
        val len = offsets[row + 1] - start
        if (len != target.size) return false
        for (i in 0 until len) {
            if (byteData[start + i] != target[i]) return false
        }
        return true
    }

    /**
     * Returns a zero-copy slice of this string buffer.
     */
    override fun slice(offset: Int, length: Int): Utf8StringBuffer {
        if (offset < 0 || length < 0 || offset + length > size) {
            throw BufferOutOfBoundsException(offset + length, size)
        }
        val slicedValidity = validity?.slice(offset, length)
        return Utf8StringBuffer(
            byteData = byteData,
            offsets = offsets,
            offsetRow = offsetRow + offset,
            size = length,
            validity = slicedValidity
        )
    }

    companion object {
        fun of(vararg values: String): Utf8StringBuffer = ofList(values.toList())

        fun ofList(values: List<String?>): Utf8StringBuffer {
            val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(values.size)
            val offsets = IntArray(values.size + 1)
            var currentByteOffset = 0

            // Pre-encode UTF-8 bytes
            val encodedList = arrayOfNulls<ByteArray>(values.size)
            values.forEachIndexed { i, str ->
                if (str != null) {
                    vBuilder.appendValid()
                    val bytes = str.encodeToByteArray()
                    encodedList[i] = bytes
                    currentByteOffset += bytes.size
                } else {
                    vBuilder.appendNull()
                }
                offsets[i + 1] = currentByteOffset
            }

            val totalBytes = ByteArray(currentByteOffset)
            var destPos = 0
            encodedList.forEach { bytes ->
                if (bytes != null) {
                    bytes.copyInto(totalBytes, destinationOffset = destPos)
                    destPos += bytes.size
                }
            }

            return Utf8StringBuffer(
                byteData = totalBytes,
                offsets = offsets,
                offsetRow = 0,
                size = values.size,
                validity = vBuilder.build()
            )
        }
    }
}
