package io.kodivx.buffer.builder

import io.kodivx.buffer.bitmap.ValidityBitmapBuilder
import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.primitive.FloatBuffer
import io.kodivx.buffer.primitive.IntBuffer
import io.kodivx.buffer.primitive.LongBuffer
import io.kodivx.buffer.string.Utf8StringBuffer
import kotlin.math.max

/**
 * Growing builder for creating [IntBuffer] instances.
 */
class IntBufferBuilder(initialCapacity: Int = 64) {
    private var data = IntArray(max(4, initialCapacity))
    private val vBuilder = ValidityBitmapBuilder(initialCapacity)
    var size: Int = 0
        private set

    private fun ensureCapacity(required: Int) {
        if (required > data.size) {
            val newCapacity = max(required, data.size * 2)
            val next = IntArray(newCapacity)
            data.copyInto(next)
            data = next
        }
    }

    fun append(value: Int) {
        ensureCapacity(size + 1)
        data[size] = value
        vBuilder.appendValid()
        size++
    }

    fun appendNull() {
        ensureCapacity(size + 1)
        data[size] = 0
        vBuilder.appendNull()
        size++
    }

    fun build(): IntBuffer {
        val finalData = IntArray(size)
        data.copyInto(finalData, endIndex = size)
        return IntBuffer(finalData, 0, size, vBuilder.build())
    }
}

/**
 * Growing builder for creating [DoubleBuffer] instances.
 */
class DoubleBufferBuilder(initialCapacity: Int = 64) {
    private var data = DoubleArray(max(4, initialCapacity))
    private val vBuilder = ValidityBitmapBuilder(initialCapacity)
    var size: Int = 0
        private set

    private fun ensureCapacity(required: Int) {
        if (required > data.size) {
            val newCapacity = max(required, data.size * 2)
            val next = DoubleArray(newCapacity)
            data.copyInto(next)
            data = next
        }
    }

    fun append(value: Double) {
        ensureCapacity(size + 1)
        data[size] = value
        vBuilder.appendValid()
        size++
    }

    fun appendNull() {
        ensureCapacity(size + 1)
        data[size] = 0.0
        vBuilder.appendNull()
        size++
    }

    fun build(): DoubleBuffer {
        val finalData = DoubleArray(size)
        data.copyInto(finalData, endIndex = size)
        return DoubleBuffer(finalData, 0, size, vBuilder.build())
    }
}

/**
 * Growing builder for creating [LongBuffer] instances.
 */
class LongBufferBuilder(initialCapacity: Int = 64) {
    private var data = LongArray(max(4, initialCapacity))
    private val vBuilder = ValidityBitmapBuilder(initialCapacity)
    var size: Int = 0
        private set

    private fun ensureCapacity(required: Int) {
        if (required > data.size) {
            val newCapacity = max(required, data.size * 2)
            val next = LongArray(newCapacity)
            data.copyInto(next)
            data = next
        }
    }

    fun append(value: Long) {
        ensureCapacity(size + 1)
        data[size] = value
        vBuilder.appendValid()
        size++
    }

    fun appendNull() {
        ensureCapacity(size + 1)
        data[size] = 0L
        vBuilder.appendNull()
        size++
    }

    fun build(): LongBuffer {
        val finalData = LongArray(size)
        data.copyInto(finalData, endIndex = size)
        return LongBuffer(finalData, 0, size, vBuilder.build())
    }
}

/**
 * Growing builder for creating [FloatBuffer] instances.
 */
class FloatBufferBuilder(initialCapacity: Int = 64) {
    private var data = FloatArray(max(4, initialCapacity))
    private val vBuilder = ValidityBitmapBuilder(initialCapacity)
    var size: Int = 0
        private set

    private fun ensureCapacity(required: Int) {
        if (required > data.size) {
            val newCapacity = max(required, data.size * 2)
            val next = FloatArray(newCapacity)
            data.copyInto(next)
            data = next
        }
    }

    fun append(value: Float) {
        ensureCapacity(size + 1)
        data[size] = value
        vBuilder.appendValid()
        size++
    }

    fun appendNull() {
        ensureCapacity(size + 1)
        data[size] = 0.0f
        vBuilder.appendNull()
        size++
    }

    fun build(): FloatBuffer {
        val finalData = FloatArray(size)
        data.copyInto(finalData, endIndex = size)
        return FloatBuffer(finalData, 0, size, vBuilder.build())
    }
}

/**
 * Growing builder for creating [Utf8StringBuffer] instances.
 */
class Utf8StringBufferBuilder(initialCapacity: Int = 64, initialByteCapacity: Int = 1024) {
    private var byteData = ByteArray(max(32, initialByteCapacity))
    private var offsets = IntArray(max(4, initialCapacity + 1))
    private val vBuilder = ValidityBitmapBuilder(initialCapacity)
    private var currentByteOffset = 0
    var size: Int = 0
        private set

    init {
        offsets[0] = 0
    }

    private fun ensureByteCapacity(additionalBytes: Int) {
        val required = currentByteOffset + additionalBytes
        if (required > byteData.size) {
            val next = ByteArray(max(required, byteData.size * 2))
            byteData.copyInto(next)
            byteData = next
        }
    }

    private fun ensureOffsetCapacity() {
        if (size + 1 >= offsets.size) {
            val next = IntArray(offsets.size * 2)
            offsets.copyInto(next)
            offsets = next
        }
    }

    fun append(value: String) {
        val bytes = value.encodeToByteArray()
        appendUtf8(bytes)
    }

    fun appendUtf8(bytes: ByteArray) {
        ensureByteCapacity(bytes.size)
        ensureOffsetCapacity()

        bytes.copyInto(byteData, destinationOffset = currentByteOffset)
        currentByteOffset += bytes.size
        offsets[size + 1] = currentByteOffset
        vBuilder.appendValid()
        size++
    }

    fun appendNull() {
        ensureOffsetCapacity()
        offsets[size + 1] = currentByteOffset
        vBuilder.appendNull()
        size++
    }

    fun build(): Utf8StringBuffer {
        val finalBytes = ByteArray(currentByteOffset)
        byteData.copyInto(finalBytes, endIndex = currentByteOffset)

        val finalOffsets = IntArray(size + 1)
        offsets.copyInto(finalOffsets, endIndex = size + 1)

        return Utf8StringBuffer(
            byteData = finalBytes,
            offsets = finalOffsets,
            offsetRow = 0,
            size = size,
            validity = vBuilder.build()
        )
    }
}
