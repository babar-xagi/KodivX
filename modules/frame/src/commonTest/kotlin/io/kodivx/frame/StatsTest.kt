package io.kodivx.frame

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StatsTest {

    @Test
    fun testVarianceAndStdDev() {
        val df = dataFrameOf(
            "x" to doubleArrayOf(10.0, 20.0, 30.0, 40.0, 50.0)
        )
        // Mean = 30.0
        // Diffs: -20, -10, 0, 10, 20
        // SqDiffs: 400 + 100 + 0 + 100 + 400 = 1000
        // Variance = 1000 / 4 = 250.0
        // StdDev = sqrt(250.0) ≈ 15.8113883
        val variance = df.variance("x")
        val stdDev = df.stdDev("x")

        assertEquals(250.0, variance)
        assertTrue(abs(stdDev - 15.8113883) < 1e-6)
    }

    @Test
    fun testQuantileAndMedian() {
        val df = dataFrameOf(
            "vals" to doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0)
        )

        assertEquals(1.0, df.quantile("vals", 0.0))
        assertEquals(2.0, df.quantile("vals", 0.25))
        assertEquals(3.0, df.median("vals"))
        assertEquals(4.0, df.quantile("vals", 0.75))
        assertEquals(5.0, df.quantile("vals", 1.0))
    }

    @Test
    fun testCovarianceAndCorrelation() {
        val df = dataFrameOf(
            "x" to doubleArrayOf(1.0, 2.0, 3.0, 4.0, 5.0),
            "y_pos" to doubleArrayOf(2.0, 4.0, 6.0, 8.0, 10.0),   // y = 2x, perfect r = 1.0
            "y_neg" to doubleArrayOf(10.0, 8.0, 6.0, 4.0, 2.0)    // y = -2x + 12, perfect r = -1.0
        )

        val rPos = df.correlation("x", "y_pos")
        val rNeg = df.correlation("x", "y_neg")

        assertTrue(abs(rPos - 1.0) < 1e-6, "Expected 1.0, got $rPos")
        assertTrue(abs(rNeg - (-1.0)) < 1e-6, "Expected -1.0, got $rNeg")

        val covPos = df.covariance("x", "y_pos")
        // x var = 2.5, y var = 10.0 -> cov = r * std_x * std_y = 1.0 * sqrt(2.5) * sqrt(10.0) = 5.0
        assertEquals(5.0, covPos)
    }

    @Test
    fun testDescribe() {
        val df = dataFrameOf(
            "name" to listOf("A", "B", "C", "D"),
            "score" to doubleArrayOf(10.0, 20.0, 30.0, 40.0),
            "age" to intArrayOf(20, 25, 30, 35)
        )

        val desc = df.describe()
        assertEquals(8, desc.rowCount)
        assertEquals(listOf("statistic", "score", "age"), desc.columnNames)

        val statCol = desc["statistic"] as StringColumn
        val scoreCol = desc["score"] as DoubleColumn
        val ageCol = desc["age"] as DoubleColumn

        val expectedStats = listOf("count", "mean", "std", "min", "25%", "50%", "75%", "max")
        for (i in 0 until 8) {
            assertEquals(expectedStats[i], statCol[i])
        }

        // Check score: count=4, mean=25, min=10, 50%=25, max=40
        assertEquals(4.0, scoreCol[0])  // count
        assertEquals(25.0, scoreCol[1]) // mean
        assertEquals(10.0, scoreCol[3]) // min
        assertEquals(25.0, scoreCol[5]) // 50%
        assertEquals(40.0, scoreCol[7]) // max

        // Check age: count=4, mean=27.5, min=20, 50%=27.5, max=35
        assertEquals(4.0, ageCol[0])
        assertEquals(27.5, ageCol[1])
        assertEquals(20.0, ageCol[3])
        assertEquals(27.5, ageCol[5])
        assertEquals(35.0, ageCol[7])
    }
}
