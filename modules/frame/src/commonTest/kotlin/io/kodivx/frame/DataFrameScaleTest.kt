package io.kodivx.frame

import kotlin.test.*

class DataFrameScaleTest {

    @Test
    fun testLargeDataFrameFilterAndAggregation() {
        val count = 100_000
        val ids = IntArray(count) { it }
        val scores = DoubleArray(count) { (it % 100).toDouble() }

        val df = dataFrameOf(
            "id" to ids,
            "score" to scores
        )

        assertEquals(count, df.rowCount)

        // Filter score >= 90.0 (10% of rows should pass)
        val filtered = df.filter { row ->
            row.getDouble("score") >= 90.0
        }

        assertEquals(10_000, filtered.rowCount)
        assertEquals(90.0, filtered.min("score"))
        assertEquals(99.0, filtered.max("score"))
    }
}
