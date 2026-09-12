package io.kodivx.buffer.bitmap

import io.kodivx.core.error.BufferOutOfBoundsException
import kotlin.math.min

/**
 * High-performance validity bitmap representing presence (bit 1) or absence/null (bit 0)
 * for a columnar buffer.
 *
 * Backed by a 64-bit [LongArray] enabling word-level SWAR (SIMD Within A Register) operations,
 * hardware population counts (`countOneBits()`), and branchless scanning.
 */
class ValidityBitmap(
    val words: LongArray,
    val size: Int,
    val offsetBits: Int = 0
) {
    init {
        require(size >= 0) { "Size must be non-negative: $size" }
        require(offsetBits >= 0) { "Offset must be non-negative: $offsetBits" }
        val requiredBits = offsetBits + size
        val requiredWords = (requiredBits + 63) ushr 6
        require(words.size >= requiredWords) {
            "Words array length (${words.size}) is insufficient for $requiredBits bits"
        }
    }

    /**
     * Checks if the element at the specified logical [index] is valid (not null).
     */
    fun isValid(index: Int): Boolean {
        if (index !in 0 until size) throw BufferOutOfBoundsException(index, size)
        val bitPos = offsetBits + index
        val wordIndex = bitPos ushr 6
        val bitInWord = bitPos and 63
        return (words[wordIndex] and (1L shl bitInWord)) != 0L
    }

    /**
     * Checks if the element at the specified logical [index] is null.
     */
    fun isNull(index: Int): Boolean = !isValid(index)

    /**
     * Fast-path check: returns true if the 64-element word starting at aligned bit index is 100% valid.
     */
    fun isWordAllValid(wordIndex: Int): Boolean = words[wordIndex] == -1L

    /**
     * Fast-path check: returns true if the 64-element word starting at aligned bit index is 100% null.
     */
    fun isWordAllNull(wordIndex: Int): Boolean = words[wordIndex] == 0L

    /**
     * Returns the total count of valid (non-null) elements in this bitmap using hardware POPCNT.
     */
    fun countValid(): Int {
        if (size == 0) return 0
        var count = 0

        // If offset is word-aligned, process full words directly
        if (offsetBits and 63 == 0) {
            val fullWords = size ushr 6
            val baseWord = offsetBits ushr 6
            for (w in 0 until fullWords) {
                count += words[baseWord + w].countOneBits()
            }
            val remainingBits = size and 63
            if (remainingBits > 0) {
                val mask = (1L shl remainingBits) - 1L
                count += (words[baseWord + fullWords] and mask).countOneBits()
            }
        } else {
            // Unaligned fallback loop
            for (i in 0 until size) {
                if (isValid(i)) count++
            }
        }
        return count
    }

    /**
     * Returns true if all elements in this bitmap are valid (contains zero nulls).
     */
    fun isAllValid(): Boolean = countValid() == size

    /**
     * Returns true if all elements in this bitmap are null.
     */
    fun isAllNull(): Boolean = countValid() == 0

    /**
     * Zero-copy slice of this validity bitmap.
     */
    fun slice(offset: Int, length: Int): ValidityBitmap {
        if (offset < 0 || length < 0 || offset + length > size) {
            throw BufferOutOfBoundsException(offset + length, size)
        }
        return ValidityBitmap(words, length, offsetBits + offset)
    }

    /**
     * Bitwise AND with another bitmap of identical size.
     */
    fun and(other: ValidityBitmap): ValidityBitmap {
        require(size == other.size) { "Bitmap sizes must match: $size vs ${other.size}" }
        val resultWordsCount = (size + 63) ushr 6
        val resultWords = LongArray(resultWordsCount)

        if ((offsetBits and 63 == 0) && (other.offsetBits and 63 == 0)) {
            val baseA = offsetBits ushr 6
            val baseB = other.offsetBits ushr 6
            for (i in 0 until resultWordsCount) {
                resultWords[i] = words[baseA + i] and other.words[baseB + i]
            }
        } else {
            val builder = ValidityBitmapBuilder(size)
            for (i in 0 until size) {
                if (isValid(i) && other.isValid(i)) builder.appendValid() else builder.appendNull()
            }
            return builder.build()
        }
        return ValidityBitmap(resultWords, size)
    }

    companion object {
        /**
         * Creates an all-valid (no nulls) bitmap of the given [size].
         */
        fun allValid(size: Int): ValidityBitmap {
            val wordCount = (size + 63) ushr 6
            val words = LongArray(wordCount) { -1L }
            val remainder = size and 63
            if (remainder > 0 && wordCount > 0) {
                words[wordCount - 1] = (1L shl remainder) - 1L
            }
            return ValidityBitmap(words, size)
        }

        /**
         * Creates an all-null bitmap of the given [size].
         */
        fun allNull(size: Int): ValidityBitmap {
            val wordCount = (size + 63) ushr 6
            return ValidityBitmap(LongArray(wordCount), size)
        }
    }
}
