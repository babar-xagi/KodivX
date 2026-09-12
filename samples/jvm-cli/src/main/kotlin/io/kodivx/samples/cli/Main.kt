package io.kodivx.samples.cli

import io.kodivx.array.*
import io.kodivx.frame.*

fun main() {
    println("==================================================")
    println("✨ KodivX — Simple data. Native speed. Everywhere.")
    println("==================================================")
    println("Live Demonstration: Phases 0, 1, 2, and 3\n")

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

    // 1. Filtering with SelectionVector
    println("\nFiltered Sales (revenue >= $8,000):")
    val highValueSales = sales.filter { row ->
        row.getDouble("revenue") >= 8000.0
    }
    println(highValueSales)

    // 2. Select & Sort
    println("\nTop Performers (Sorted by Revenue Descending):")
    val sortedSummary = sales
        .select("rep", "region", "deals", "revenue")
        .sortBy("revenue", ascending = false)
    println(sortedSummary)

    // 3. Simple Aggregations
    println("\nAggregations:")
    println("   Total Transactions: ${sales.count()}")
    println("   Total Revenue:      $${sales.sum("revenue")}")
    println("   Average Deal Size:  $${sales.mean("revenue")}")
    println("   Top Deal Value:     $${sales.max("revenue")}")
    println("   Smallest Deal:      $${sales.min("revenue")}")

    // 4. Head & Tail
    println("\nTop 2 Deals (head):")
    println(sortedSummary.head(2))

    // ----------------------------------------------------
    // Performance Scale Benchmark
    // ----------------------------------------------------
    println("\n--- [3. High-Throughput Scale Benchmark] ---")
    val rowCount = 100_000
    val testIds = IntArray(rowCount) { it }
    val testScores = DoubleArray(rowCount) { (it % 500).toDouble() }

    val largeDf = dataFrameOf(
        "id" to testIds,
        "score" to testScores
    )

    val start = System.currentTimeMillis()
    val filteredLarge = largeDf.filter { row ->
        row.getDouble("score") >= 400.0
    }
    val avgScore = filteredLarge.mean("score")
    val duration = System.currentTimeMillis() - start

    println("⚡ Filtered $rowCount rows -> ${filteredLarge.rowCount} matches in ${duration}ms")
    println("   Average filtered score = $avgScore")
    println("\n✅ Phase 3 DataFrame MVP Verified Successfully!")
}
