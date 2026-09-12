package io.kodivx.array

import kotlin.test.*

class ArrayScaleTest {

    @Test
    fun testLargeVectorAddition() {
        val count = 1_000_000
        val a = DoubleVector(DoubleArray(count) { 2.0 })
        val b = DoubleVector(DoubleArray(count) { 3.0 })

        val c = a + b
        assertEquals(count, c.size)
        assertEquals(5.0, c[0])
        assertEquals(5.0, c[count - 1])
    }

    @Test
    fun testLargeVectorDotProduct() {
        val count = 1_000_000
        val a = DoubleVector(DoubleArray(count) { 1.0 })
        val b = DoubleVector(DoubleArray(count) { 2.0 })

        val dot = a dot b
        assertEquals(2_000_000.0, dot)
    }

    @Test
    fun testStridedSlicingScale() {
        val count = 1_000_000
        val a = DoubleVector(DoubleArray(count) { it.toDouble() })

        // Slicing every 10th element: 100,000 elements
        val sub = a.slice(0, count, step = 10)
        assertEquals(100_000, sub.size)
        assertEquals(0.0, sub[0])
        assertEquals(10.0, sub[1])
        assertEquals(999_990.0, sub[99_999])
    }
}
