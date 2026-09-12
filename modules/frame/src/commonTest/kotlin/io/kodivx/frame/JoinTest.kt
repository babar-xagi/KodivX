package io.kodivx.frame

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JoinTest {

    private fun leftUsersDf(): DataFrame = dataFrameOf(
        "id" to intArrayOf(1, 2, 3, 4),
        "name" to listOf("Ali", "Sara", "Babar", "Zara"),
        "dept" to listOf("Eng", "Sales", "Eng", "HR")
    )

    private fun rightOrdersDf(): DataFrame = dataFrameOf(
        "order_id" to intArrayOf(101, 102, 103, 104),
        "id" to intArrayOf(1, 2, 2, 5), // Babar (3) and Zara (4) have no orders; id=5 has no user
        "amount" to doubleArrayOf(250.0, 100.0, 150.0, 300.0)
    )

    @Test
    fun testInnerJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Inner)

        // Matches:
        // user 1 -> order 101
        // user 2 -> order 102
        // user 2 -> order 103
        // Total rows: 3
        assertEquals(3, joined.rowCount)
        assertEquals(listOf("id", "name", "dept", "order_id", "amount"), joined.columnNames)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn
        val orderIds = joined["order_id"] as IntColumn
        val amounts = joined["amount"] as DoubleColumn

        assertEquals(1, ids[0])
        assertEquals("Ali", names[0])
        assertEquals(101, orderIds[0])
        assertEquals(250.0, amounts[0])

        assertEquals(2, ids[1])
        assertEquals("Sara", names[1])
        assertEquals(102, orderIds[1])
        assertEquals(100.0, amounts[1])

        assertEquals(2, ids[2])
        assertEquals("Sara", names[2])
        assertEquals(103, orderIds[2])
        assertEquals(150.0, amounts[2])
    }

    @Test
    fun testLeftJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Left)

        // Rows:
        // user 1 -> order 101
        // user 2 -> order 102
        // user 2 -> order 103
        // user 3 (Babar) -> null order
        // user 4 (Zara) -> null order
        // Total rows: 5
        assertEquals(5, joined.rowCount)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn
        val orderIds = joined["order_id"] as IntColumn
        val amounts = joined["amount"] as DoubleColumn

        assertEquals("Babar", names[3])
        assertTrue(orderIds.isNull(3))
        assertTrue(amounts.isNull(3))

        assertEquals("Zara", names[4])
        assertTrue(orderIds.isNull(4))
        assertTrue(amounts.isNull(4))
    }

    @Test
    fun testRightJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Right)

        // Rows:
        // user 1 -> order 101
        // user 2 -> order 102
        // user 2 -> order 103
        // null user -> order 104 (id 5)
        // Total rows: 4
        assertEquals(4, joined.rowCount)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn
        val orderIds = joined["order_id"] as IntColumn

        assertEquals(5, ids[3])
        assertTrue(names.isNull(3))
        assertEquals(104, orderIds[3])
    }

    @Test
    fun testFullJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Full)

        // 3 matched rows + 2 unmatched left (3, 4) + 1 unmatched right (5) = 6 rows
        assertEquals(6, joined.rowCount)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn
        val orderIds = joined["order_id"] as IntColumn

        // Check unmatched left (Babar, Zara)
        assertEquals("Babar", names[3])
        assertTrue(orderIds.isNull(3))

        assertEquals("Zara", names[4])
        assertTrue(orderIds.isNull(4))

        // Check unmatched right (id 5)
        assertEquals(5, ids[5])
        assertTrue(names.isNull(5))
        assertEquals(104, orderIds[5])
    }

    @Test
    fun testSemiJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Semi)

        // Semi join returns only left rows that have at least one match in right.
        // Left columns only! No duplicate left rows!
        assertEquals(2, joined.rowCount)
        assertEquals(listOf("id", "name", "dept"), joined.columnNames)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn

        assertEquals(1, ids[0])
        assertEquals("Ali", names[0])

        assertEquals(2, ids[1])
        assertEquals("Sara", names[1])
    }

    @Test
    fun testAntiJoin() {
        val left = leftUsersDf()
        val right = rightOrdersDf()

        val joined = left.join(right, on = "id", how = JoinType.Anti)

        // Anti join returns left rows that have NO match in right table.
        // Babar (3) and Zara (4)
        assertEquals(2, joined.rowCount)
        assertEquals(listOf("id", "name", "dept"), joined.columnNames)

        val ids = joined["id"] as IntColumn
        val names = joined["name"] as StringColumn

        assertEquals(3, ids[0])
        assertEquals("Babar", names[0])

        assertEquals(4, ids[1])
        assertEquals("Zara", names[1])
    }

    @Test
    fun testColumnCollisionWithSuffix() {
        val left = dataFrameOf(
            "id" to intArrayOf(1, 2),
            "score" to doubleArrayOf(80.0, 90.0)
        )
        val right = dataFrameOf(
            "id" to intArrayOf(1, 2),
            "score" to doubleArrayOf(85.0, 95.0)
        )

        val joined = left.join(right, on = "id", suffix = "_new")
        assertEquals(listOf("id", "score", "score_new"), joined.columnNames)

        val scoreLeft = joined["score"] as DoubleColumn
        val scoreRight = joined["score_new"] as DoubleColumn

        assertEquals(80.0, scoreLeft[0])
        assertEquals(85.0, scoreRight[0])
    }

    @Test
    fun testDifferentJoinKeys() {
        val left = dataFrameOf(
            "user_id" to intArrayOf(10, 20),
            "name" to listOf("Alpha", "Beta")
        )
        val right = dataFrameOf(
            "account_id" to intArrayOf(10, 20),
            "balance" to doubleArrayOf(1000.0, 2000.0)
        )

        val joined = left.join(right, leftOn = "user_id", rightOn = "account_id")
        assertEquals(2, joined.rowCount)
        assertEquals(listOf("user_id", "name", "account_id", "balance"), joined.columnNames)

        val balance = joined["balance"] as DoubleColumn
        assertEquals(1000.0, balance[0])
        assertEquals(2000.0, balance[1])
    }
}
