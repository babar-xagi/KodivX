package io.kodivx.core

import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.schema.Field
import io.kodivx.core.schema.Schema
import io.kodivx.core.schema.schema
import io.kodivx.core.type.DataType
import io.kodivx.core.type.Scalar
import kotlin.test.*

class CoreTypesAndSchemaTest {

    @Test
    fun testDataTypeProperties() {
        assertTrue(DataType.Int32.isNumeric)
        assertTrue(DataType.Float64.isNumeric)
        assertTrue(DataType.Int32.isPrimitive)
        assertFalse(DataType.Utf8.isNumeric)
        assertFalse(DataType.Utf8.isPrimitive)
        assertEquals(4, DataType.Int32.byteWidth)
        assertEquals(8, DataType.Float64.byteWidth)
    }

    @Test
    fun testScalarCreation() {
        val intScalar = Scalar.of(42)
        assertEquals(DataType.Int32, intScalar.type)
        assertEquals(42, intScalar.rawValue)
        assertFalse(intScalar.isNull)

        val nullScalar = Scalar.nullOf(DataType.Float64)
        assertEquals(DataType.Float64, nullScalar.type)
        assertNull(nullScalar.rawValue)
        assertTrue(nullScalar.isNull)

        val stringScalar = Scalar.of("KodivX")
        assertEquals(DataType.Utf8, stringScalar.type)
        assertEquals("KodivX", stringScalar.rawValue)
    }

    @Test
    fun testSchemaDsl() {
        val userSchema = schema {
            int("id", nullable = false)
            string("name")
            double("balance")
            boolean("is_active")
        }

        assertEquals(4, userSchema.size)
        assertEquals(listOf("id", "name", "balance", "is_active"), userSchema.fieldNames)

        val idField = userSchema["id"]
        assertEquals(DataType.Int32, idField.type)
        assertFalse(idField.nullable)

        val nameField = userSchema["name"]
        assertEquals(DataType.Utf8, nameField.type)
        assertTrue(nameField.nullable)

        assertEquals(1, userSchema.indexOf("name"))
        assertTrue(userSchema.contains("balance"))
        assertFalse(userSchema.contains("unknown"))
    }

    @Test
    fun testSchemaOperations() {
        val base = schema {
            int("a")
            double("b")
        }

        val extended = base.withField(Field("c", DataType.Utf8))
        assertEquals(3, extended.size)
        assertTrue(extended.contains("c"))

        val dropped = extended.dropField("b")
        assertEquals(2, dropped.size)
        assertFalse(dropped.contains("b"))

        val selected = extended.select("c", "a")
        assertEquals(listOf("c", "a"), selected.fieldNames)
    }

    @Test
    fun testSchemaValidation() {
        assertFailsWith<IllegalArgumentException> {
            schema {
                int("duplicate")
                double("duplicate")
            }
        }

        val s = schema { int("x") }
        assertFailsWith<ColumnNotFoundException> {
            s["non_existent"]
        }
    }
}
