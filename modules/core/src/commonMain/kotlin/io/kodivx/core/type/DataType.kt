package io.kodivx.core.type

/**
 * Represents logical data types supported in KodivX columns and scalar expressions.
 */
sealed interface DataType {
    val displayName: String
    val isNumeric: kotlin.Boolean get() = false
    val isPrimitive: kotlin.Boolean get() = false
    val byteWidth: Int get() = -1

    data object Boolean : DataType {
        override val displayName: String get() = "Boolean"
        override val isPrimitive: kotlin.Boolean get() = true
        override val byteWidth: Int get() = 1
        override fun toString(): String = displayName
    }

    data object Int32 : DataType {
        override val displayName: String get() = "Int32"
        override val isNumeric: kotlin.Boolean get() = true
        override val isPrimitive: kotlin.Boolean get() = true
        override val byteWidth: Int get() = 4
        override fun toString(): String = displayName
    }

    data object Int64 : DataType {
        override val displayName: String get() = "Int64"
        override val isNumeric: kotlin.Boolean get() = true
        override val isPrimitive: kotlin.Boolean get() = true
        override val byteWidth: Int get() = 8
        override fun toString(): String = displayName
    }

    data object Float32 : DataType {
        override val displayName: String get() = "Float32"
        override val isNumeric: kotlin.Boolean get() = true
        override val isPrimitive: kotlin.Boolean get() = true
        override val byteWidth: Int get() = 4
        override fun toString(): String = displayName
    }

    data object Float64 : DataType {
        override val displayName: String get() = "Float64"
        override val isNumeric: kotlin.Boolean get() = true
        override val isPrimitive: kotlin.Boolean get() = true
        override val byteWidth: Int get() = 8
        override fun toString(): String = displayName
    }

    data object Utf8 : DataType {
        override val displayName: String get() = "Utf8"
        override fun toString(): String = displayName
    }

    data object Binary : DataType {
        override val displayName: String get() = "Binary"
        override fun toString(): String = displayName
    }

    companion object {
        val Bool = Boolean
        val Int = Int32
        val Long = Int64
        val Float = Float32
        val Double = Float64
        val String = Utf8
    }
}
