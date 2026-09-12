package io.kodivx.frame

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LazyDataFrameTest {

    private fun sampleSalesDf(): DataFrame = dataFrameOf(
        "id" to intArrayOf(1, 2, 3, 4, 5),
        "name" to listOf("Ali", "Sara", "Babar", "Zara", "John"),
        "dept" to listOf("Eng", "Sales", "Eng", "Sales", "Eng"),
        "age" to intArrayOf(25, 17, 30, 16, 28),
        "score" to doubleArrayOf(85.0, 92.0, 78.0, 95.0, 88.0)
    )

    @Test
    fun testEagerVsLazyEquivalence() {
        val df = sampleSalesDf()

        // Eager execution
        val eager = df.filter { row ->
            row.getInt("age") >= 18
        }.select("name", "age", "score")

        // Lazy execution
        val lazyResult = df.lazy()
            .filter(col("age").gte(18))
            .select("name", "age", "score")
            .collect()

        assertEquals(eager.rowCount, lazyResult.rowCount)
        assertEquals(eager.columnNames, lazyResult.columnNames)

        val eagerNames = eager["name"] as StringColumn
        val lazyNames = lazyResult["name"] as StringColumn

        for (i in 0 until eager.rowCount) {
            assertEquals(eagerNames[i], lazyNames[i])
        }
    }

    @Test
    fun testLazyGroupByAndAgg() {
        val df = sampleSalesDf()

        val grouped = df.lazy()
            .groupBy("dept")
            .agg(
                sum("score").alias("total_score"),
                mean("score").alias("avg_score"),
                count().alias("count")
            )
            .sortBy("total_score", ascending = false)
            .collect()

        assertEquals(2, grouped.rowCount)
        assertEquals(listOf("dept", "total_score", "avg_score", "count"), grouped.columnNames)

        val deptCol = grouped["dept"] as StringColumn
        val totalCol = grouped["total_score"] as DoubleColumn
        val countCol = grouped["count"] as IntColumn

        assertEquals("Eng", deptCol[0])
        assertEquals(251.0, totalCol[0]) // 85 + 78 + 88
        assertEquals(3, countCol[0])

        assertEquals("Sales", deptCol[1])
        assertEquals(187.0, totalCol[1]) // 92 + 95
        assertEquals(2, countCol[1])
    }

    @Test
    fun testLazyJoin() {
        val left = dataFrameOf(
            "id" to intArrayOf(1, 2, 3),
            "name" to listOf("Ali", "Sara", "Babar")
        )
        val right = dataFrameOf(
            "id" to intArrayOf(1, 2, 4),
            "role" to listOf("Dev", "Lead", "Designer")
        )

        val joined = left.lazy()
            .join(right.lazy(), on = "id", how = JoinType.Inner)
            .collect()

        assertEquals(2, joined.rowCount)
        assertEquals(listOf("id", "name", "role"), joined.columnNames)

        val names = joined["name"] as StringColumn
        assertEquals("Ali", names[0])
        assertEquals("Sara", names[1])
    }

    @Test
    fun testOptimizerCombinesAdjacentFilters() {
        val df = sampleSalesDf()

        val lazyQuery = df.lazy()
            .filter(col("age").gte(18))
            .filter(col("score").gt(80.0))

        val explainOutput = lazyQuery.explain(showOptimized = true)

        // Logical plan has 2 Filter nodes
        assertTrue(explainOutput.contains("=== Logical Plan ==="))
        assertTrue(explainOutput.contains("Filter: (col(\"age\") >= 18)"))
        assertTrue(explainOutput.contains("Filter: (col(\"score\") > 80.0)"))

        // Optimized plan combines them with AND
        assertTrue(explainOutput.contains("=== Optimized Plan ==="))
        assertTrue(explainOutput.contains("Filter: ((col(\"age\") >= 18) AND (col(\"score\") > 80.0))"))

        // Execution produces correct result
        val result = lazyQuery.collect()
        assertEquals(2, result.rowCount) // Ali (25, 85) and John (28, 88)
    }

    @Test
    fun testOptimizerCombinesAdjacentLimits() {
        val df = sampleSalesDf()

        val query = df.lazy()
            .limit(4)
            .limit(2)

        val explainOutput = query.explain(showOptimized = true)
        assertTrue(explainOutput.contains("Limit: offset=0, count=2"))

        val result = query.collect()
        assertEquals(2, result.rowCount)
    }

    @Test
    fun testExplainDiagnostics() {
        val df = sampleSalesDf()

        val query = df.lazy()
            .filter(col("score").gte(80.0))
            .select("name", "score")

        val explain = query.explain()
        assertTrue(explain.contains("=== Logical Plan ==="))
        assertTrue(explain.contains("Projection: [col(\"name\"), col(\"score\")]"))
        assertTrue(explain.contains("Filter: (col(\"score\") >= 80.0)"))
        assertTrue(explain.contains("Scan: DataFrame(5 rows, 5 cols)"))
    }
}
