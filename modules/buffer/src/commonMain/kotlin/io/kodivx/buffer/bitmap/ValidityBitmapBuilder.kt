package io.kodivx.buffer.bitmap

import kotlin.math.max

/**
 * Growing builder for constructing [ValidityBitmap] instances efficiently.
 */
class ValidityBitmapBuilder(initialCapacity: Int = 64) {
    private var words = LongArray(max(1, (initialCapacity + 63) ushr 6))
    var size: Int = 0
        private set

    private fun ensureCapacity(requiredSize: Int) {
        val requiredWords = (requiredSize + 63) ushr 6
        if (requiredWords > words.size) {
            val newWordCapacity = max(requiredWords, words.size * 2)
            val newWords = LongArray(newWordCapacity)
            words.copyInto(newWords)
            words = newWords
        }
    }

    fun appendValid() {
        ensureCapacity(size + 1)
        val wordIndex = size ushr 6
        val bitIndex = size and 63
        words[wordIndex] = words[wordIndex] or (1L shl bitIndex)
        size++
    }

    fun appendNull() {
        ensureCapacity(size + 1)
        val wordIndex = size ushr 6
        val bitIndex = size and 63
        words[wordIndex] = words[wordIndex] and (1L shl bitIndex).inv()
        size++
    }

    fun append(isValid: Boolean) {
        if (isValid) appendValid() else appendNull()
    }

    fun appendAllValid(count: Int) {
        require(count >= 0) { "Count must be non-negative: $count" }
        if (count == 0) return
        ensureCapacity(size + count)
        for (i in 0 until count) {
            appendValid()
        }
    }

    fun build(): ValidityBitmap {
        val wordCount = (size + 63) ushr 6
        val finalWords = LongArray(wordCount)
        words.copyInto(finalWords, endIndex = wordCount)
        return ValidityBitmap(finalWords, size)
    }
}
