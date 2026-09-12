package io.kodivx.array

import kotlin.test.*

class MatrixTest {

    @Test
    fun testMatrixCreationAndAccess() {
        val m = matrixOf(
            2, 3,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        )
        assertEquals(2, m.rows)
        assertEquals(3, m.cols)
        assertEquals(6, m.size)
        assertEquals(1.0, m[0, 0])
        assertEquals(3.0, m[0, 2])
        assertEquals(6.0, m[1, 2])

        // Zero-copy row view
        val r1 = m.row(1)
        assertEquals(3, r1.size)
        assertEquals(4.0, r1[0])
        assertEquals(6.0, r1[2])

        // Zero-copy col view
        val c2 = m.col(2)
        assertEquals(2, c2.size)
        assertEquals(3.0, c2[0])
        assertEquals(6.0, c2[1])
    }

    @Test
    fun testMatrixTranspose() {
        val m = matrixOf(
            2, 3,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        )
        val t = m.transpose()

        assertEquals(3, t.rows)
        assertEquals(2, t.cols)
        assertEquals(1.0, t[0, 0])
        assertEquals(4.0, t[0, 1])
        assertEquals(2.0, t[1, 0])
        assertEquals(5.0, t[1, 1])
        assertEquals(3.0, t[2, 0])
        assertEquals(6.0, t[2, 1])

        // Double transpose restores original
        val t2 = t.transpose()
        assertEquals(2, t2.rows)
        assertEquals(3, t2.cols)
        assertEquals(m[1, 2], t2[1, 2])
    }

    @Test
    fun testMatrixBroadcasting() {
        val m = matrixOf(
            2, 2,
            10.0, 20.0,
            30.0, 40.0
        )
        val v = array(1.0, 2.0)

        // Broadcast vector v to each row of m
        val res = m + v
        assertEquals(11.0, res[0, 0])
        assertEquals(22.0, res[0, 1])
        assertEquals(31.0, res[1, 0])
        assertEquals(42.0, res[1, 1])
    }

    @Test
    fun testMatrixMultiplication() {
        // A: 2x3
        val a = matrixOf(
            2, 3,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        )
        // B: 3x2
        val b = matrixOf(
            3, 2,
            7.0, 8.0,
            9.0, 1.0,
            2.0, 3.0
        )

        // C = A * B -> 2x2
        // C[0,0] = 1*7 + 2*9 + 3*2 = 7 + 18 + 6 = 31
        // C[0,1] = 1*8 + 2*1 + 3*3 = 8 + 2 + 9 = 19
        // C[1,0] = 4*7 + 5*9 + 6*2 = 28 + 45 + 12 = 85
        // C[1,1] = 4*8 + 5*1 + 6*3 = 32 + 5 + 18 = 55
        val c = a matmul b
        assertEquals(2, c.rows)
        assertEquals(2, c.cols)
        assertEquals(31.0, c[0, 0])
        assertEquals(19.0, c[0, 1])
        assertEquals(85.0, c[1, 0])
        assertEquals(55.0, c[1, 1])
    }

    @Test
    fun testAxisReductions() {
        val m = matrixOf(
            2, 3,
            1.0, 2.0, 3.0,
            4.0, 5.0, 6.0
        )

        // Sum across rows (axis 0) -> column sums [5.0, 7.0, 9.0]
        val colSums = m.sum(axis = 0)
        assertEquals(3, colSums.size)
        assertEquals(5.0, colSums[0])
        assertEquals(7.0, colSums[1])
        assertEquals(9.0, colSums[2])

        // Sum across columns (axis 1) -> row sums [6.0, 15.0]
        val rowSums = m.sum(axis = 1)
        assertEquals(2, rowSums.size)
        assertEquals(6.0, rowSums[0])
        assertEquals(15.0, rowSums[1])
    }

    @Test
    fun testIdentityMatrix() {
        val i = eye(3)
        assertEquals(3, i.rows)
        assertEquals(3, i.cols)
        assertEquals(1.0, i[0, 0])
        assertEquals(0.0, i[0, 1])
        assertEquals(1.0, i[1, 1])
        assertEquals(1.0, i[2, 2])

        val v = array(3.0, 5.0, 7.0)
        val res = i dot v
        assertEquals(v[0], res[0])
        assertEquals(v[1], res[1])
        assertEquals(v[2], res[2])
    }
}
