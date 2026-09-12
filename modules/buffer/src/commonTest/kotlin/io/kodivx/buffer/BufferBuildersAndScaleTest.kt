package io.kodivx.buffer

import io.kodivx.buffer.builder.DoubleBufferBuilder
import io.kodivx.buffer.builder.IntBufferBuilder
import io.kodivx.buffer.builder.Utf8StringBufferBuilder
import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.primitive.IntBuffer
import kotlin.test.*

class BufferBuildersAndScaleTest {

    @Test
    fun testIntBufferBuilderGrowth() {
        val builder = IntBufferBuilder(initialCapacity = 4)
        for (i in 0 until 100) {
            if (i % 5 == 0) builder.appendNull() else builder.append(i * 10)
        }
        val buffer = builder.build()
        assertEquals(100, buffer.size)
        assertEquals(20, buffer.nullCount)

        for (i in 0 until 100) {
            if (i % 5 == 0) {
                assertTrue(buffer.isNull(i))
            } else {
                assertTrue(buffer.isValid(i))
                assertEquals(i * 10, buffer.getInt(i))
            }
        }
    }

    @Test
    fun testUtf8StringBufferBuilderGrowth() {
        val builder = Utf8StringBufferBuilder(initialCapacity = 2, initialByteCapacity = 8)
        builder.append("alpha")
        builder.appendNull()
        builder.append("beta_longer_string_exceeding_initial_byte_capacity")
        builder.append("gamma")

        val buffer = builder.build()
        assertEquals(4, buffer.size)
        assertEquals("alpha", buffer.getString(0))
        assertNull(buffer.getString(1))
        assertEquals("beta_longer_string_exceeding_initial_byte_capacity", buffer.getString(2))
        assertEquals("gamma", buffer.getString(3))
    }

    /**
     * Phase 1 Exit Criteria Test:
     * Millions of primitive values can be stored and scanned without allocating
     * one Kotlin object per scalar value.
     */
    @Test
    fun testOneMillionElementsScanningScale() {
        val count = 1_000_000
        val rawData = IntArray(count) { it }
        val buffer = IntBuffer(rawData)

        assertEquals(count, buffer.size)
        assertFalse(buffer.isNullable)

        var sum = 0L
        // Scan 1,000,000 elements in unboxed primitive loop
        buffer.forEachInt { value ->
            sum += value
        }

        val expectedSum = (count.toLong() * (count - 1)) / 2
        assertEquals(expectedSum, sum)
    }

    @Test
    fun testOneMillionElementsDoubleReduction() {
        val count = 1_000_000
        val rawData = DoubleArray(count) { 1.0 }
        val buffer = DoubleBuffer(rawData)

        var sum = 0.0
        buffer.forEachDouble { value ->
            sum += value
        }

        assertEquals(count.toDouble(), sum, 0.001)
    }
}
