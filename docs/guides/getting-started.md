# Getting Started with KodivX

Welcome to KodivX! This guide walks you through setting up KodivX in your project and running your first analytical queries.

---

## 1. Installation

### With Gradle (Kotlin Multiplatform / JVM)

In your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.kodivx:kodivx:0.1.0")
}
```

### With Qutivex

```powershell
qutivex init my-data-project
cd my-data-project
qutivex add io.kodivx:kodivx@0.1.0
```

---

## 2. Working with Arrays

KodivX provides fast, memory-compact numerical vectors and matrices:

```kotlin
import io.kodivx.array.*

fun main() {
    val a = array(1.0, 2.0, 3.0, 4.0)
    val b = array(10.0, 20.0, 30.0, 40.0)

    val c = a + b
    println("Mean: ${c.mean()}")
    println("Sum: ${c.sum()}")

    // 2D Matrix reshaping and operations
    val matrix = c.reshape(2, 2)
    println("Matrix shape: ${matrix.shape}")
}
```

---

## 3. Working with DataFrames

Construct DataFrames from collections, primitive arrays, or file sources:

```kotlin
import io.kodivx.frame.*

fun main() {
    val df = dataFrameOf(
        "id" to intArrayOf(1, 2, 3, 4),
        "product" to listOf("Laptop", "Mouse", "Keyboard", "Monitor"),
        "price" to doubleArrayOf(1200.0, 25.0, 75.0, 300.0),
        "in_stock" to booleanArrayOf(true, true, false, true)
    )

    // Filter, select, and sort
    val premiumInStock = df
        .filter(col<Double>("price") > 50.0 and col<Boolean>("in_stock"))
        .select("product", "price")
        .sortBy(desc("price"))

    println(premiumInStock)
}
```

---

## 4. GroupBy & Aggregations

Perform fast hash-based aggregations:

```kotlin
val summary = sales
    .groupBy("region")
    .agg(
        count().alias("transaction_count"),
        sum("revenue").alias("total_revenue"),
        mean("revenue").alias("avg_revenue")
    )
```

---

## 5. Lazy Query Execution

For large datasets, defer execution so KodivX can optimize query plans before running:

```kotlin
val lazyQuery = df.lazy()
    .filter(col<Double>("revenue") > 1000.0)
    .select("customer_id", "revenue")

// Inspect the optimized query plan
println(lazyQuery.explain())

// Trigger vectorized execution
val result = lazyQuery.collect()
```
