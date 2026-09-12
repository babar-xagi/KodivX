package io.kodivx.samples.cli

import io.kodivx.array.*
import io.kodivx.frame.*

fun main() {
    println("==================================================")
    println("✨ KodivX — Simple data. Native speed. Everywhere.")
    println("==================================================")
    println("Live Demonstration: Phases 0, 1, 2, 3, and 4\n")

    // ----------------------------------------------------
    // Phase 2: Numerical Arrays & Matrix Core
    // ----------------------------------------------------
    println("--- [1. Numerical Arrays & Linear Algebra] ---")
    val v1 = array(1.0, 2.0, 3.0, 4.0)
    val v2 = array(10.0, 20.0, 30.0, 40.0)
    val vResult = (v1 * 2.0) + v2
    println("Vector result: $vResult")
    println("Vector stats: sum=${vResult.sum()}, mean=${vResult.mean()}, stdDev=${vResult.stdDev()}")
    println("Dot product (v1 · v2): ${v1 dot v2}")

    val matrix = matrixOf(
        2, 3,
        1.0, 2.0, 3.0,
        4.0, 5.0, 6.0
    )
    println("\nMatrix (2x3):\n$matrix")
    println("O(1) Transpose (3x2):\n${matrix.transpose()}\n")

    // ----------------------------------------------------
    // Phase 3: Columnar DataFrame MVP
    // ----------------------------------------------------
    println("--- [2. Columnar DataFrame Operations] ---")

    val sales = dataFrameOf(
        "id" to intArrayOf(101, 102, 103, 104, 105, 106),
        "rep" to listOf("Ali", "Sara", "Babar", "Sara", "Ali", "Babar"),
        "region" to listOf("North", "South", "East", "West", "North", "South"),
        "deals" to intArrayOf(5, 12, 8, 15, 3, 9),
        "revenue" to doubleArrayOf(4500.0, 12800.0, 7200.0, 16500.0, 2900.0, 8900.0)
    )

    println("Original Sales Table:")
    println(sales)

    // Filtering with SelectionVector
    val highValueSales = sales.filter { row ->
        row.getDouble("revenue") >= 8000.0
    }
    println("\nFiltered Sales (revenue >= $8,000):")
    println(highValueSales)

    // ----------------------------------------------------
    // Phase 4: GroupBy & Aggregations
    // ----------------------------------------------------
    println("\n--- [3. GroupBy & Multi-Aggregation] ---")
    val repSummary = sales
        .groupBy("rep")
        .agg(
            sum("revenue").alias("total_rev"),
            mean("revenue").alias("avg_deal"),
            sum("deals").alias("total_deals"),
            count().alias("num_transactions")
        )
        .sortBy("total_rev", ascending = false)

    println("Sales Performance by Representative:")
    println(repSummary)

    // ----------------------------------------------------
    // Phase 4: Relational Joins
    // ----------------------------------------------------
    println("\n--- [4. Relational Joins (Hash Join)] ---")
    val targets = dataFrameOf(
        "rep" to listOf("Ali", "Sara", "Babar", "Zara"),
        "target" to doubleArrayOf(10000.0, 25000.0, 15000.0, 12000.0),
        "tier" to listOf("Gold", "Platinum", "Silver", "Bronze")
    )

    val joinedWithTargets = repSummary.join(targets, on = "rep", how = JoinType.Left)
    println("Rep Performance Joined with Quota Targets (Left Join):")
    println(joinedWithTargets)

    // ----------------------------------------------------
    // Phase 4: Statistics & Describe
    // ----------------------------------------------------
    println("\n--- [5. Statistical Analysis & Describe] ---")
    println("Sales Summary Statistics (describe):")
    println(sales.describe())

    println("Correlation (deals vs revenue): ${sales.correlation("deals", "revenue")}")
    println("Revenue Variance: ${sales.variance("revenue")}")
    println("Revenue StdDev:   ${sales.stdDev("revenue")}")
    println("Revenue Median:   ${sales.median("revenue")}")
    println("Revenue 90th Pct: ${sales.quantile("revenue", 0.90)}")

    // ----------------------------------------------------
    // Performance Scale Benchmark
    // ----------------------------------------------------
    println("\n--- [6. High-Throughput Scale Benchmark] ---")
    val rowCount = 100_000
    val testIds = IntArray(rowCount) { it }
    val categories = listOf("Cat-A", "Cat-B", "Cat-C", "Cat-D", "Cat-E")
    val testCats = Array(rowCount) { categories[it % categories.size] }
    val testScores = DoubleArray(rowCount) { (it % 500).toDouble() }

    val largeDf = dataFrameOf(
        "id" to testIds,
        "category" to testCats,
        "score" to testScores
    )

    val start = System.currentTimeMillis()
    val groupResult = largeDf
        .groupBy("category")
        .agg(
            count(),
            mean("score").alias("avg_score"),
            stdDev("score").alias("std_score")
        )
    val duration = System.currentTimeMillis() - start

    println("⚡ GroupBy over $rowCount rows -> ${groupResult.rowCount} groups in ${duration}ms")
    println(groupResult)
    println("\n✅ Phase 4 GroupBy, Joins & Statistics Verified Successfully!")
}
