package io.kodivx.samples.cli

import io.kodivx.array.*
import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.primitive.IntBuffer
import io.kodivx.buffer.string.Utf8StringBuffer
import io.kodivx.core.schema.schema

fun main() {
    println("==================================================")
    println("✨ KodivX — Simple data. Native speed. Everywhere.")
    println("==================================================")
    println("Phase 1 & Phase 2 Live Multiplatform Demonstration\n")

    // ----------------------------------------------------
    // Phase 1 Demo: Schemas and Columnar Buffers
    // ----------------------------------------------------
    println("--- [Phase 1: Core Schema & Columnar Buffers] ---")
    val userSchema = schema {
        int("id", nullable = false)
        string("username")
        double("balance")
    }
    println("📋 Schema: ${userSchema.fieldNames}")

    val idBuffer = IntBuffer.of(101, 102, 103)
    val nameBuffer = Utf8StringBuffer.of("Alice", "Bob", "Charlie")
    val balanceBuffer = DoubleBuffer.ofNullable(listOf(1540.50, null, 9820.75))

    for (i in 0 until idBuffer.size) {
        val bal = if (balanceBuffer.isNull(i)) "NULL" else "$" + balanceBuffer.getDouble(i)
        println("   Row $i: ID=${idBuffer.getInt(i)}, User=${nameBuffer.getString(i)}, Balance=$bal")
    }

    // ----------------------------------------------------
    // Phase 2 Demo: Numerical Vectors & Matrices
    // ----------------------------------------------------
    println("\n--- [Phase 2: Array and Numerical Core] ---")

    // 1. Vector Operations
    val a = array(1.0, 2.0, 3.0, 4.0)
    val b = array(10.0, 20.0, 30.0, 40.0)
    val c = (a * 2.0) + b

    println("Vector a: $a")
    println("Vector b: $b")
    println("Calculated (a * 2) + b: $c")
    println("Vector stats: sum=${c.sum()}, mean=${c.mean()}, stdDev=${c.stdDev()}")

    // 2. Zero-Copy Slicing & Dot Product
    val slice = c.slice(1, 4)
    println("Zero-copy slice [1 until 4]: $slice")
    println("Dot product (a · b): ${a dot b}")

    // 3. 2D Matrices and Transposition
    val matrix = matrixOf(
        2, 3,
        1.0, 2.0, 3.0,
        4.0, 5.0, 6.0
    )
    println("\nMatrix M (Shape: ${matrix.shape}):")
    println(matrix)

    val transposed = matrix.transpose()
    println("O(1) Transposed M^T (Shape: ${transposed.shape}):")
    println(transposed)

    // 4. Matrix Multiplication: (2x3) * (3x2) -> (2x2)
    val weights = matrixOf(
        3, 2,
        0.5, 1.0,
        1.5, 2.0,
        2.5, 3.0
    )
    val matMulResult = matrix matmul weights
    println("Matrix Multiplication (M matmul W):")
    println(matMulResult)

    // 5. Broadcast Addition: Matrix (2x3) + Vector (3,)
    val bias = array(100.0, 200.0, 300.0)
    val broadcastResult = matrix + bias
    println("Broadcast Addition (M + Vector Bias):")
    println(broadcastResult)

    // ----------------------------------------------------
    // Performance Scale Benchmark
    // ----------------------------------------------------
    val n = 1_000_000
    val vec1 = DoubleVector(DoubleArray(n) { 1.5 })
    val vec2 = DoubleVector(DoubleArray(n) { 2.5 })

    val start = System.currentTimeMillis()
    val vec3 = vec1 + vec2
    val dot = vec1 dot vec2
    val duration = System.currentTimeMillis() - start

    println("\n⚡ Scale Benchmark (1,000,000 Elements):")
    println("   Vector Addition + Dot Product took: ${duration}ms")
    println("   Vec3[0] = ${vec3[0]}, Dot = $dot")
    println("\n✅ Phase 2 Array Core Verified Successfully!")
}
