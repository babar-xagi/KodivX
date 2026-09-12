package io.kodivx.core.type

/**
 * Represents a typed scalar literal or constant value in KodivX expressions and queries.
 */
sealed interface Scalar {
    val type: DataType
    val isNull: kotlin.Boolean get() = false
    val rawValue: Any?

    data class Bool(val value: kotlin.Boolean) : Scalar {
        override val type: DataType get() = DataType.Boolean
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = value.toString()
    }

    data class Int(val value: kotlin.Int) : Scalar {
        override val type: DataType get() = DataType.Int32
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = value.toString()
    }

    data class Long(val value: kotlin.Long) : Scalar {
        override val type: DataType get() = DataType.Int64
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = value.toString()
    }

    data class Float(val value: kotlin.Float) : Scalar {
        override val type: DataType get() = DataType.Float32
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = value.toString()
    }

    data class Double(val value: kotlin.Double) : Scalar {
        override val type: DataType get() = DataType.Float64
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = value.toString()
    }

    data class Utf8(val value: kotlin.String) : Scalar {
        override val type: DataType get() = DataType.Utf8
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = "\"$value\""
    }

    data class Binary(val value: ByteArray) : Scalar {
        override val type: DataType get() = DataType.Binary
        override val rawValue: Any get() = value
        override fun toString(): kotlin.String = "[Binary ${value.size} bytes]"

        override fun equals(other: Any?): kotlin.Boolean {
            if (this === other) return true
            if (other !is Binary) return false
            return value.contentEquals(other.value)
        }

        override fun hashCode(): kotlin.Int = value.contentHashCode()
    }

    data class Null(override val type: DataType) : Scalar {
        override val isNull: kotlin.Boolean get() = true
        override val rawValue: Any? get() = null
        override fun toString(): kotlin.String = "null($type)"
    }

    companion object {
        fun of(value: kotlin.Boolean): Scalar = Bool(value)
        fun of(value: kotlin.Int): Scalar = Int(value)
        fun of(value: kotlin.Long): Scalar = Long(value)
        fun of(value: kotlin.Float): Scalar = Float(value)
        fun of(value: kotlin.Double): Scalar = Double(value)
        fun of(value: kotlin.String): Scalar = Utf8(value)
        fun of(value: ByteArray): Scalar = Binary(value)
        fun nullOf(type: DataType): Scalar = Null(type)
    }
}
