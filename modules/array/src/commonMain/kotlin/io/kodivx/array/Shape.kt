package io.kodivx.array

/**
 * Represents the dimensional shape of an N-dimensional array or matrix.
 */
class Shape(vararg val dimensions: Int) {

    init {
        for (d in dimensions) {
            require(d >= 0) { "Dimension size must be non-negative: $d in ${dimensions.contentToString()}" }
        }
    }

    val rank: Int get() = dimensions.size

    val size: Int get() = if (dimensions.isEmpty()) 0 else dimensions.fold(1) { acc, dim -> acc * dim }

    val is1D: Boolean get() = rank == 1
    val is2D: Boolean get() = rank == 2

    operator fun get(axis: Int): Int = dimensions[axis]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Shape) return false
        return dimensions.contentEquals(other.dimensions)
    }

    override fun hashCode(): Int = dimensions.contentHashCode()

    override fun toString(): String = buildString {
        append("(")
        append(dimensions.joinToString(", "))
        if (rank == 1) append(",")
        append(")")
    }

    companion object {
        fun of(vararg dims: Int): Shape = Shape(*dims)
        val SCALAR = Shape()
    }
}
