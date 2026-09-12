package io.kodivx.array

import kotlin.math.abs
import kotlin.test.*

class VectorTest {

    @Test
    fun testVectorCreationAndIndexing() {
        val v = array(1.0, 2.0, 3.0, 4.0)
        assertEquals(4, v.size)
        assertEquals(Shape(4), v.shape)
        assertEquals(1.0, v[0])
        assertEquals(4.0, v[3])
    }

    @Test
    fun testVectorSlicing() {
        val v = array(10.0, 20.0, 30.0, 40.0, 50.0)
        val slice = v.slice(1, 4) // [20.0, 30.0, 40.0]
        assertEquals(3, slice.size)
        assertEquals(20.0, slice[0])
        assertEquals(40.0, slice[2])

        // Strided slice with step 2: [10.0, 30.0, 50.0]
        val strided = v.slice(0, 5, step = 2)
        assertEquals(3, strided.size)
        assertEquals(10.0, strided[0])
        assertEquals(30.0, strided[1])
        assertEquals(50.0, strided[2])
    }

    @Test
    fun testVectorArithmetic() {
        val a = array(1.0, 2.0, 3.0)
        val b = array(10.0, 20.0, 30.0)

        val add = a + b
        assertEquals(11.0, add[0])
        assertEquals(22.0, add[1])
        assertEquals(33.0, add[2])

        val sub = b - a
        assertEquals(9.0, sub[0])
        assertEquals(18.0, sub[1])
        assertEquals(27.0, sub[2])

        val mul = a * 2.0
        assertEquals(2.0, mul[0])
        assertEquals(4.0, mul[1])
        assertEquals(6.0, mul[2])

        val div = b / 10.0
        assertEquals(1.0, div[0])
        assertEquals(2.0, div[1])
        assertEquals(3.0, div[2])
    }

    @Test
    fun testReductions() {
        val v = array(1.0, 2.0, 3.0, 4.0, 5.0)
        assertEquals(15.0, v.sum())
        assertEquals(3.0, v.mean())
        assertEquals(1.0, v.min())
        assertEquals(5.0, v.max())

        val variance = v.variance(isSample = true)
        assertEquals(2.5, variance, 0.0001)

        val stdDev = v.stdDev(isSample = true)
        assertTrue(abs(stdDev - 1.581138) < 0.001)
    }

    @Test
    fun testDotProductAndNorm() {
        val a = array(1.0, 2.0, 3.0)
        val b = array(4.0, 5.0, 6.0)

        // 1*4 + 2*5 + 3*6 = 4 + 10 + 18 = 32
        val dot = a dot b
        assertEquals(32.0, dot)

        val c = array(3.0, 4.0)
        assertEquals(5.0, c.norm())
    }

    @Test
    fun testReshape() {
        val v = array(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)
        val m = v.reshape(2, 3)

        assertEquals(2, m.rows)
        assertEquals(3, m.cols)
        assertEquals(1.0, m[0, 0])
        assertEquals(3.0, m[0, 2])
        assertEquals(4.0, m[1, 0])
        assertEquals(6.0, m[1, 2])
    }

    @Test
    fun testLinspace() {
        val space = linspace(0.0, 10.0, 5)
        assertEquals(5, space.size)
        assertEquals(0.0, space[0])
        assertEquals(2.5, space[1])
        assertEquals(5.0, space[2])
        assertEquals(7.5, space[3])
        assertEquals(10.0, space[4])
    }
}
