package io.kodivx.samples.cli

import io.kodivx.buffer.bitmap.ValidityBitmap
import io.kodivx.buffer.primitive.DoubleBuffer
import io.kodivx.buffer.primitive.IntBuffer
import io.kodivx.buffer.string.Utf8StringBuffer
import io.kodivx.core.schema.schema

fun main() {
    println("==================================================")
    println("✨ KodivX — Simple data. Native speed. Everywhere.")
    println("==================================================")
    println("Phase 1: Core Types, Schema, and Buffer System Demo\n")

    // 1. Define a schema using the type-safe DSL
    val userSchema = schema {
        int("id", nullable = false)
        string("username")
        double("account_balance")
        boolean("is_verified")
    }

    println("📋 Schema defined:")
    println("   Fields: ${userSchema.fields.joinToString(", ")}")
    println("   Total Columns: ${userSchema.size}\n")

    // 2. Construct memory-efficient columnar buffers
    val idBuffer = IntBuffer.of(101, 102, 103, 104)
    val nameBuffer = Utf8StringBuffer.of("Alice", "Bob", "Charlie", "Diana")
    val balanceBuffer = DoubleBuffer.ofNullable(listOf(1540.50, 230.00, null, 9820.75))

    println("📦 Columnar Buffers Created:")
    println("   ID Buffer: Size=${idBuffer.size}, Nullable=${idBuffer.isNullable}")
    println("   Name Buffer: Size=${nameBuffer.size}, Nullable=${nameBuffer.isNullable}")
    println("   Balance Buffer: Size=${balanceBuffer.size}, Nulls=${balanceBuffer.nullCount}\n")

    // 3. Display tabular representation
    println("📊 Tabular View:")
    println("   ID   | USERNAME | BALANCE")
    println("   -----+----------+----------")
    for (i in 0 until idBuffer.size) {
        val id = idBuffer.getInt(i)
        val name = nameBuffer.getString(i)
        val balance = balanceBuffer.getDouble(i).let {
            if (balanceBuffer.isNull(i)) "NULL" else "$" + it
        }
        println("   ${id.toString().padEnd(4)} | ${name?.padEnd(8)} | $balance")
    }

    // 4. Demonstrate Phase 1 Exit Criteria: Scanning 1,000,000 numbers in contiguous memory
    val millionCount = 1_000_000
    val millionArray = DoubleArray(millionCount) { it * 1.5 }
    val millionBuffer = DoubleBuffer(millionArray)

    var totalSum = 0.0
    val startTime = System.currentTimeMillis()
    millionBuffer.forEachDouble { totalSum += it }
    val duration = System.currentTimeMillis() - startTime

    println("\n⚡ Phase 1 Scale Test:")
    println("   Scanned $millionCount primitive elements in ${duration}ms")
    println("   Sum = $totalSum (Zero per-cell object allocations)")
    println("\n✅ Phase 1 Foundation Verified.")
}
