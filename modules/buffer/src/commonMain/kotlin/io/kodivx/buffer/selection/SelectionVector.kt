package io.kodivx.buffer.selection

import io.kodivx.core.error.BufferOutOfBoundsException

/**
 * Selection vector representing an active subset of row indices in a batch or chunk.
 *
 * Enables zero-copy filtering, sorting, and projection operations without re-allocating
 * or copying underlying columnar buffers until terminal materialization.
 */
class SelectionVector(
    val indices: IntArray,
    val offset: Int = 0,
    val length: Int = indices.size - offset
) {
    init {
        require(offset >= 0) { "Offset must be non-negative: $offset" }
        require(length >= 0) { "Length must be non-negative: $length" }
        require(offset + length <= indices.size) {
            "Offset ($offset) + length ($length) exceeds indices size (${indices.size})"
        }
    }

    operator fun get(index: Int): Int {
        if (index !in 0 until length) throw BufferOutOfBoundsException(index, length)
        return indices[offset + index]
    }

    fun slice(offset: Int, len: Int): SelectionVector {
        if (offset < 0 || len < 0 || offset + len > length) {
            throw BufferOutOfBoundsException(offset + len, length)
        }
        return SelectionVector(indices, this.offset + offset, len)
    }

    inline fun forEachIndex(action: (Int) -> Unit) {
        val start = offset
        val end = start + length
        for (i in start until end) {
            action(indices[i])
        }
    }

    companion object {
        fun of(vararg indices: Int): SelectionVector = SelectionVector(indices)

        fun identity(size: Int): SelectionVector {
            val arr = IntArray(size) { it }
            return SelectionVector(arr)
        }
    }
}
