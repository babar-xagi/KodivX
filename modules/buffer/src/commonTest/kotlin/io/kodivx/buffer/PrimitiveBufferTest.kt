package io.kodivx.buffer

import io.kodivx.buffer.primitive.*
import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.buffer.string.Utf8StringBuffer
import kotlin.test.*

class PrimitiveBufferTest {

    @Test
    fun testIntBuffer() {
        val buf = IntBuffer.of(10, 20, 30, 40, 50)
        assertEquals(5, buf.size)
        assertEquals(10, buf.getInt(0))
        assertEquals(50, buf.getInt(4))

        var sum = 0
        buf.forEachInt { sum += it }
        assertEquals(150, sum)

        // Slicing without copying
        val slice = buf.slice(1, 3)
        assertEquals(3, slice.size)
        assertEquals(20, slice.getInt(0))
        assertEquals(40, slice.getInt(2))
    }

    @Test
    fun testNullableDoubleBuffer() {
        val buf = DoubleBuffer.ofNullable(listOf(1.5, null, 3.5, null, 5.5))
        assertEquals(5, buf.size)
        assertEquals(2, buf.nullCount)

        assertEquals(1.5, buf.getDouble(0))
        assertTrue(buf.isNull(1))
        assertEquals(3.5, buf.getDouble(2))
        assertTrue(buf.isNull(3))
        assertEquals(5.5, buf.getDouble(4))

        assertNull(buf.getBoxed(1))
        assertEquals(3.5, buf.getBoxed(2))

        val slice = buf.slice(2, 3)
        assertEquals(3, slice.size)
        assertEquals(3.5, slice.getDouble(0))
        assertTrue(slice.isNull(1))
        assertEquals(5.5, slice.getDouble(2))
    }

    @Test
    fun testBooleanBuffer() {
        val bools = BooleanBuffer.of(true, false, true, true, false)
        assertEquals(5, bools.size)
        assertTrue(bools.getBoolean(0))
        assertFalse(bools.getBoolean(1))
        assertTrue(bools.getBoolean(2))
        assertTrue(bools.getBoolean(3))
        assertFalse(bools.getBoolean(4))

        val slice = bools.slice(2, 2)
        assertEquals(2, slice.size)
        assertTrue(slice.getBoolean(0))
        assertTrue(slice.getBoolean(1))
    }

    @Test
    fun testUtf8StringBuffer() {
        val strings = Utf8StringBuffer.of("Hello", "KodivX", "Fast", "Columnar")
        assertEquals(4, strings.size)
        assertEquals("Hello", strings.getString(0))
        assertEquals("KodivX", strings.getString(1))

        // Test zero-allocation byte equality
        val target = "KodivX".encodeToByteArray()
        assertTrue(strings.equalsUtf8(1, target))
        assertFalse(strings.equalsUtf8(0, target))

        val slice = strings.slice(1, 2)
        assertEquals(2, slice.size)
        assertEquals("KodivX", slice.getString(0))
        assertEquals("Fast", slice.getString(1))
    }

    @Test
    fun testSelectionVector() {
        val data = IntBuffer.of(100, 200, 300, 400, 500)
        // Select elements at index 1 and 3 (200, 400)
        val sel = SelectionVector.of(1, 3)
        assertEquals(2, sel.length)
        assertEquals(1, sel[0])
        assertEquals(3, sel[1])

        val selectedValues = mutableListOf<Int>()
        sel.forEachIndex { idx ->
            selectedValues.add(data.getInt(idx))
        }
        assertEquals(listOf(200, 400), selectedValues)
    }
}
