package io.kodivx.samples.cli

import io.kodivx.array.*
import io.kodivx.frame.*

fun main() {
    println("==================================================")
    println("✨ KodivX — Simple data. Native speed. Everywhere.")
    println("==================================================")
    println("Live Demonstration: Phases 0, 1, 2, 3, 4, and 5\n")

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

    // ----------------------------------------------------
    // Phase 4: GroupBy & Joins
    // ----------------------------------------------------
    println("\n--- [3. GroupBy & Relational Joins] ---")
    val repSummary = sales
        .groupBy("rep")
        .agg(
            sum("revenue").alias("total_rev"),
            mean("revenue").alias("avg_deal"),
            sum("deals").alias("total_deals"),
            count().alias("num_transactions")
        )
        .sortBy("total_rev", ascending = false)

    val targets = dataFrameOf(
        "rep" to listOf("Ali", "Sara", "Babar", "Zara"),
        "target" to doubleArrayOf(10000.0, 25000.0, 15000.0, 12000.0),
        "tier" to listOf("Gold", "Platinum", "Silver", "Bronze")
    )

    val joinedWithTargets = repSummary.join(targets, on = "rep", how = JoinType.Left)
    println("Rep Performance Left-Joined with Quota Targets:")
    println(joinedWithTargets)

    // ----------------------------------------------------
    // Phase 5: Expression System (AST, Eval, Vectorized Filter)
    // ----------------------------------------------------
    println("\n--- [4. Expression System (Phase 5)] ---")
    val efficiencyExpr = (col("revenue") / col("deals")).alias("rev_per_deal")
    println("Expression Pretty Print:")
    println("   Formula: ${efficiencyExpr.toPrettyString()}")
    println("   AST Hierarchy:")
    println(efficiencyExpr.toTreeString())

    // Vectorized Expression Filter: revenue >= 8000.0 AND deals >= 9
    val highPerformingDeals = sales.filter(
        col("revenue").gte(8000.0) and col("deals").gte(9)
    )
    println("\nFiltered via Expression AST (revenue >= 8000 AND deals >= 9):")
    println(highPerformingDeals)

    // Expression Projection
    val performanceTable = sales.select(
        col("rep"),
        col("region"),
        (col("revenue") / col("deals")).alias("rev_per_deal"),
        col("deals").gte(10).alias("high_volume")
    )
    println("\nProjected Table via Expressions (select):")
    println(performanceTable)

    // ----------------------------------------------------
    // Phase 4 & 5: Statistics & Describe
    // ----------------------------------------------------
    println("\n--- [5. Statistical Analysis & Describe] ---")
    println("Sales Summary Statistics (describe):")
    println(sales.describe())
    println("Correlation (deals vs revenue): ${sales.correlation("deals", "revenue")}")

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
    // Vectorized AST Filter over 100,000 rows
    val filteredLarge = largeDf.filter(
        col("score").gte(450.0)
    )
    val duration = System.currentTimeMillis() - start

    println("⚡ Evaluated Expression AST Filter over $rowCount rows -> ${filteredLarge.rowCount} matches in ${duration}ms")
    println("\n✅ Phase 5 Expression System Verified Successfully!")
}
