package io.kodivx.frame

import io.kodivx.core.error.ColumnNotFoundException
import kotlin.test.*

class DataFrameTest {

    @Test
    fun testDataFrameCreationAndAccess() {
        val df = dataFrameOf(
            "name" to listOf("Ali", "Sara", "Babar"),
            "age" to intArrayOf(21, 24, 23),
            "score" to doubleArrayOf(81.0, 95.0, 90.0)
        )

        assertEquals(3, df.rowCount)
        assertEquals(3, df.columnCount)
        assertEquals(listOf("name", "age", "score"), df.columnNames)

        val nameCol = df["name"]
        assertEquals("Sara", nameCol[1])

        val ageCol = df["age"] as IntColumn
        assertEquals(21, ageCol.getInt(0))
        assertEquals(23, ageCol.getInt(2))

        val r1 = df.row(1)
        assertEquals("Sara", r1.getString("name"))
        assertEquals(24, r1.getInt("age"))
        assertEquals(95.0, r1.getDouble("score"))
    }

    @Test
    fun testSelectAndDrop() {
        val df = dataFrameOf(
            "a" to intArrayOf(1, 2),
            "b" to doubleArrayOf(1.0, 2.0),
            "c" to listOf("x", "y")
        )

        val selected = df.select("c", "a")
        assertEquals(2, selected.columnCount)
        assertEquals(listOf("c", "a"), selected.columnNames)
        assertEquals(2, selected.rowCount)

        val dropped = df.drop("b")
        assertEquals(2, dropped.columnCount)
        assertEquals(listOf("a", "c"), dropped.columnNames)
    }

    @Test
    fun testWithColumnAndRename() {
        val df = dataFrameOf(
            "x" to intArrayOf(10, 20),
            "y" to doubleArrayOf(1.5, 2.5)
        )

        val withBonus = df.withColumn(
            DoubleColumn("bonus", io.kodivx.buffer.primitive.DoubleBuffer.of(5.0, 5.0))
        )
        assertEquals(3, withBonus.columnCount)
        assertEquals(listOf("x", "y", "bonus"), withBonus.columnNames)

        val renamed = withBonus.rename("x" to "id", "bonus" to "extra")
        assertEquals(listOf("id", "y", "extra"), renamed.columnNames)
    }

    @Test
    fun testHeadTailLimit() {
        val df = dataFrameOf(
            "id" to intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        )

        val top3 = df.head(3)
        assertEquals(3, top3.rowCount)
        assertEquals(1, (top3["id"] as IntColumn).getInt(0))
        assertEquals(3, (top3["id"] as IntColumn).getInt(2))

        val last2 = df.tail(2)
        assertEquals(2, last2.rowCount)
        assertEquals(9, (last2["id"] as IntColumn).getInt(0))
        assertEquals(10, (last2["id"] as IntColumn).getInt(1))
    }

    @Test
    fun testFilter() {
        val users = dataFrameOf(
            "name" to listOf("Ali", "Sara", "Babar", "John"),
            "score" to doubleArrayOf(75.0, 95.0, 90.0, 60.0)
        )

        // Filter score >= 90.0
        val topScorers = users.filter { row ->
            row.getDouble("score") >= 90.0
        }

        assertEquals(2, topScorers.rowCount)
        val names = topScorers["name"]
        assertEquals("Sara", names[0])
        assertEquals("Babar", names[1])
    }

    @Test
    fun testSorting() {
        val df = dataFrameOf(
            "name" to listOf("Charlie", "Alice", "Bob"),
            "score" to doubleArrayOf(70.0, 95.0, 85.0)
        )

        // Sort by score descending
        val sorted = df.sortBy("score", ascending = false)
        assertEquals("Alice", sorted["name"][0])
        assertEquals("Bob", sorted["name"][1])
        assertEquals("Charlie", sorted["name"][2])

        // Sort by score ascending
        val ascSorted = df.sortBy("score", ascending = true)
        assertEquals("Charlie", ascSorted["name"][0])
        assertEquals("Bob", ascSorted["name"][1])
        assertEquals("Alice", ascSorted["name"][2])
    }

    @Test
    fun testAggregations() {
        val df = dataFrameOf(
            "score" to doubleArrayOf(10.0, 20.0, 30.0, 40.0)
        )

        assertEquals(4, df.count())
        assertEquals(100.0, df.sum("score"))
        assertEquals(25.0, df.mean("score"))
        assertEquals(10.0, df.min("score"))
        assertEquals(40.0, df.max("score"))
    }

    @Test
    fun testConsoleTableFormatting() {
        val df = dataFrameOf(
            "name" to listOf("Alpha", "Beta"),
            "val" to intArrayOf(1, 2)
        )

        val table = df.toString()
        assertTrue(table.contains("DataFrame: 2 rows x 2 cols"))
        assertTrue(table.contains("name"))
        assertTrue(table.contains("Alpha"))
        assertTrue(table.contains("Beta"))
    }
}
