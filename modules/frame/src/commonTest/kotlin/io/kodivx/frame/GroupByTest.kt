package io.kodivx.frame

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GroupByTest {

    private fun sampleSalesDf(): DataFrame = dataFrameOf(
        "dept" to listOf("Engineering", "Sales", "Engineering", "Sales", "HR"),
        "region" to listOf("US", "US", "EU", "EU", "US"),
        "salary" to doubleArrayOf(120.0, 90.0, 110.0, 95.0, 80.0),
        "bonus" to doubleArrayOf(15.0, 10.0, 12.0, 15.0, 5.0)
    )

    @Test
    fun testBasicGroupByAndCount() {
        val df = sampleSalesDf()
        val grouped = df.groupBy("dept").count()

        assertEquals(3, grouped.rowCount)
        assertEquals(listOf("dept", "count"), grouped.columnNames)

        val deptCol = grouped["dept"] as StringColumn
        val countCol = grouped["count"] as IntColumn

        assertEquals("Engineering", deptCol[0])
        assertEquals(2, countCol[0])

        assertEquals("Sales", deptCol[1])
        assertEquals(2, countCol[1])

        assertEquals("HR", deptCol[2])
        assertEquals(1, countCol[2])
    }

    @Test
    fun testMultiAggregationsWithAlias() {
        val df = sampleSalesDf()
        val result = df.groupBy("dept").agg(
            sum("salary").alias("total_salary"),
            mean("salary").alias("avg_salary"),
            min("salary").alias("min_salary"),
            max("salary").alias("max_salary"),
            count().alias("headcount")
        )

        assertEquals(3, result.rowCount)
        assertEquals(listOf("dept", "total_salary", "avg_salary", "min_salary", "max_salary", "headcount"), result.columnNames)

        val deptCol = result["dept"] as StringColumn
        val totalCol = result["total_salary"] as DoubleColumn
        val avgCol = result["avg_salary"] as DoubleColumn
        val minCol = result["min_salary"] as DoubleColumn
        val maxCol = result["max_salary"] as DoubleColumn
        val countCol = result["headcount"] as IntColumn

        // Engineering: 120.0 + 110.0 = 230.0, mean = 115.0, min = 110.0, max = 120.0, count = 2
        assertEquals("Engineering", deptCol[0])
        assertEquals(230.0, totalCol[0])
        assertEquals(115.0, avgCol[0])
        assertEquals(110.0, minCol[0])
        assertEquals(120.0, maxCol[0])
        assertEquals(2, countCol[0])

        // Sales: 90.0 + 95.0 = 185.0, mean = 92.5, min = 90.0, max = 95.0, count = 2
        assertEquals("Sales", deptCol[1])
        assertEquals(185.0, totalCol[1])
        assertEquals(92.5, avgCol[1])
        assertEquals(90.0, minCol[1])
        assertEquals(95.0, maxCol[1])
        assertEquals(2, countCol[1])

        // HR: 80.0
        assertEquals("HR", deptCol[2])
        assertEquals(80.0, totalCol[2])
        assertEquals(80.0, avgCol[2])
        assertEquals(80.0, minCol[2])
        assertEquals(80.0, maxCol[2])
        assertEquals(1, countCol[2])
    }

    @Test
    fun testVarianceAndStdDevAggregation() {
        val df = dataFrameOf(
            "group" to listOf("A", "A", "A", "B"),
            "val" to doubleArrayOf(2.0, 4.0, 6.0, 10.0) // A: mean=4.0, var=((2-4)^2 + (4-4)^2 + (6-4)^2)/2 = (4+0+4)/2 = 4.0, std = 2.0
        )

        val result = df.groupBy("group").agg(
            variance("val").alias("var"),
            stdDev("val").alias("std")
        )

        assertEquals(2, result.rowCount)
        val varCol = result["var"] as DoubleColumn
        val stdCol = result["std"] as DoubleColumn

        assertEquals(4.0, varCol[0])
        assertEquals(2.0, stdCol[0])

        // For group B with 1 element, variance and std should be null
        assertTrue(varCol.isNull(1))
        assertTrue(stdCol.isNull(1))
    }

    @Test
    fun testMultiColumnGroupBy() {
        val df = sampleSalesDf()
        val result = df.groupBy("dept", "region").agg(sum("salary"))

        // (Engineering, US), (Sales, US), (Engineering, EU), (Sales, EU), (HR, US) -> 5 distinct groups
        assertEquals(5, result.rowCount)
        assertEquals(listOf("dept", "region", "sum(salary)"), result.columnNames)
    }

    @Test
    fun testGroupByShortcuts() {
        val df = sampleSalesDf()
        val meanDf = df.groupBy("dept").mean("salary", "bonus")

        assertEquals(3, meanDf.rowCount)
        assertEquals(listOf("dept", "mean(salary)", "mean(bonus)"), meanDf.columnNames)

        val minDf = df.groupBy("dept").min("salary")
        assertEquals(3, minDf.rowCount)
        assertEquals(listOf("dept", "min(salary)"), minDf.columnNames)

        val maxDf = df.groupBy("dept").max("salary")
        assertEquals(3, maxDf.rowCount)
        assertEquals(listOf("dept", "max(salary)"), maxDf.columnNames)
    }

    @Test
    fun testEmptyDataFrameGroupBy() {
        val df = dataFrameOf(
            "cat" to emptyList<String>(),
            "val" to doubleArrayOf()
        )
        val result = df.groupBy("cat").agg(count(), sum("val"))
        assertEquals(0, result.rowCount)
        assertEquals(listOf("cat", "count", "sum(val)"), result.columnNames)
    }
}
