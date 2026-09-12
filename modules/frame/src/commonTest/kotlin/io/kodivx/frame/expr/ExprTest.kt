package io.kodivx.frame

import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.schema.Schema
import io.kodivx.core.type.DataType
import io.kodivx.frame.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ExprTest {

    private fun sampleDf(): DataFrame = dataFrameOf(
        "name" to listOf("Ali", "Sara", "Babar", "Zara"),
        "age" to intArrayOf(25, 17, 30, 16),
        "score" to doubleArrayOf(85.5, 92.0, 78.0, 95.5),
        "active" to booleanArrayOf(true, false, true, true)
    )

    @Test
    fun testAstConstructionAndPrettyPrinting() {
        val expr = (col("price") * col("quantity")).gt(1000)
        assertEquals("((col(\"price\") * col(\"quantity\")) > 1000)", expr.toPrettyString())

        val tree = expr.toTreeString()
        assertTrue(tree.contains("GreaterThan"))
        assertTrue(tree.contains("Multiply"))
        assertTrue(tree.contains("Column(price)"))
        assertTrue(tree.contains("Column(quantity)"))
        assertTrue(tree.contains("Literal(1000)"))
    }

    @Test
    fun testTypeInferenceAndValidation() {
        val schema = io.kodivx.core.schema.schema {
            int("id")
            string("name")
            double("score")
            boolean("active")
        }

        val numExpr = col("score") * 2.0 + 10
        assertEquals(DataType.Float64, numExpr.inferType(schema))

        val boolExpr = col("score").gte(80.0) and col("active")
        assertEquals(DataType.Boolean, boolExpr.inferType(schema))

        val strExpr = col("name").upper()
        assertEquals(DataType.Utf8, strExpr.inferType(schema))

        // Validate succeeds
        numExpr.validate(schema)

        // Type mismatch: cannot do boolean AND numeric
        val invalidExpr = col("active") and col("score")
        assertFailsWith<IllegalArgumentException> {
            invalidExpr.validate(schema)
        }

        // Missing column
        val missingExpr = col("non_existent").gt(10)
        assertFailsWith<ColumnNotFoundException> {
            missingExpr.validate(schema)
        }
    }

    @Test
    fun testDataFrameFilterWithExpr() {
        val df = sampleDf()

        // Filter: age >= 18 and score > 80.0
        val adultsWithHighScore = df.filter(
            col("age").gte(18) and col("score").gt(80.0)
        )

        // Only Ali (age 25, score 85.5) matches
        assertEquals(1, adultsWithHighScore.rowCount)
        assertEquals("Ali", (adultsWithHighScore["name"] as StringColumn)[0])
    }

    @Test
    fun testDataFrameSelectWithExpr() {
        val df = sampleDf()

        val projected = df.select(
            col("name"),
            (col("score") * 2.0).alias("double_score"),
            col("age").gte(18).alias("is_adult")
        )

        assertEquals(4, projected.rowCount)
        assertEquals(listOf("name", "double_score", "is_adult"), projected.columnNames)

        val doubleScore = projected["double_score"] as DoubleColumn
        val isAdult = projected["is_adult"] as BooleanColumn

        assertEquals(171.0, doubleScore[0]) // 85.5 * 2
        assertTrue(isAdult.getBoolean(0))   // 25 >= 18

        assertEquals(184.0, doubleScore[1]) // 92.0 * 2
        assertEquals(false, isAdult.getBoolean(1)) // 17 < 18
    }

    @Test
    fun testDataFrameWithColumnExpr() {
        val df = sampleDf()

        val updated = df
            .withColumn("is_adult", col("age").gte(18))
            .withColumn("name_upper", col("name").upper())

        assertEquals(6, updated.columnCount)
        assertTrue(updated.columnNames.contains("is_adult"))
        assertTrue(updated.columnNames.contains("name_upper"))

        val upperCol = updated["name_upper"] as StringColumn
        assertEquals("ALI", upperCol[0])
        assertEquals("SARA", upperCol[1])
        assertEquals("BABAR", upperCol[2])
    }

    @Test
    fun testStringExpressions() {
        val df = sampleDf()

        val filtered = df.filter(col("name").startsWith("S") or col("name").endsWith("r"))
        // Sara (starts with S), Babar (ends with r) -> 2 rows
        assertEquals(2, filtered.rowCount)

        val names = filtered["name"] as StringColumn
        assertEquals("Sara", names[0])
        assertEquals("Babar", names[1])
    }

    @Test
    fun testGroupByWithExprAggregations() {
        val df = dataFrameOf(
            "dept" to listOf("Eng", "Sales", "Eng", "Sales"),
            "salary" to doubleArrayOf(100.0, 80.0, 120.0, 90.0)
        )

        val result = df.groupBy("dept").agg(
            sum(col("salary")).alias("total_sal"),
            mean(col("salary")).alias("avg_sal"),
            count().alias("headcount")
        )

        assertEquals(2, result.rowCount)
        assertEquals(listOf("dept", "total_sal", "avg_sal", "headcount"), result.columnNames)

        val totalSal = result["total_sal"] as DoubleColumn
        assertEquals(220.0, totalSal[0]) // Eng
        assertEquals(170.0, totalSal[1]) // Sales
    }
}
