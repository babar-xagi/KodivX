package io.kodivx.buffer

import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.buffer.bitmap.ValidityBitmapBuilder
import kotlin.test.*

class ValidityBitmapTest {

    @Test
    fun testAllValidBitmap() {
        val bitmap = ValidityBitmap.allValid(130)
        assertEquals(130, bitmap.size)
        assertEquals(130, bitmap.countValid())
        assertTrue(bitmap.isAllValid())
        assertFalse(bitmap.isAllNull())

        for (i in 0 until 130) {
            assertTrue(bitmap.isValid(i))
            assertFalse(bitmap.isNull(i))
        }
    }

    @Test
    fun testAllNullBitmap() {
        val bitmap = ValidityBitmap.allNull(130)
        assertEquals(130, bitmap.size)
        assertEquals(0, bitmap.countValid())
        assertFalse(bitmap.isAllValid())
        assertTrue(bitmap.isAllNull())

        for (i in 0 until 130) {
            assertFalse(bitmap.isValid(i))
            assertTrue(bitmap.isNull(i))
        }
    }

    @Test
    fun testValidityBitmapBuilder() {
        val builder = ValidityBitmapBuilder()
        builder.appendValid()
        builder.appendNull()
        builder.appendValid()
        builder.appendValid()
        builder.appendNull()

        val bitmap = builder.build()
        assertEquals(5, bitmap.size)
        assertEquals(3, bitmap.countValid())

        assertTrue(bitmap.isValid(0))
        assertTrue(bitmap.isNull(1))
        assertTrue(bitmap.isValid(2))
        assertTrue(bitmap.isValid(3))
        assertTrue(bitmap.isNull(4))
    }

    @Test
    fun testBitmapAcrossWordBoundaries() {
        // Test across multiple 64-bit words
        val builder = ValidityBitmapBuilder()
        for (i in 0 until 150) {
            if (i % 3 == 0) builder.appendNull() else builder.appendValid()
        }

        val bitmap = builder.build()
        assertEquals(150, bitmap.size)

        var expectedValid = 0
        for (i in 0 until 150) {
            if (i % 3 == 0) {
                assertTrue(bitmap.isNull(i), "Index $i should be null")
            } else {
                assertTrue(bitmap.isValid(i), "Index $i should be valid")
                expectedValid++
            }
        }
        assertEquals(expectedValid, bitmap.countValid())
    }

    @Test
    fun testBitmapSlicing() {
        val builder = ValidityBitmapBuilder()
        for (i in 0 until 100) {
            builder.append(i >= 50)
        }
        val full = builder.build()

        // Slicing second half (all valid)
        val slice1 = full.slice(50, 50)
        assertEquals(50, slice1.size)
        assertEquals(50, slice1.countValid())
        assertTrue(slice1.isAllValid())

        // Slicing first half (all null)
        val slice2 = full.slice(0, 50)
        assertEquals(50, slice2.size)
        assertEquals(0, slice2.countValid())
        assertTrue(slice2.isAllNull())
    }

    @Test
    fun testBitwiseAnd() {
        val b1 = ValidityBitmapBuilder().apply {
            appendValid(); appendNull(); appendValid(); appendNull()
        }.build()

        val b2 = ValidityBitmapBuilder().apply {
            appendValid(); appendValid(); appendNull(); appendNull()
        }.build()

        val combined = b1.and(b2)
        assertEquals(4, combined.size)
        assertTrue(combined.isValid(0))
        assertFalse(combined.isValid(1))
        assertFalse(combined.isValid(2))
        assertFalse(combined.isValid(3))
        assertEquals(1, combined.countValid())
    }
}
