package io.kodivx.core.error

/**
 * Base exception for all KodivX runtime and engine errors.
 */
open class KodivxException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

class ColumnNotFoundException(
    columnName: String,
    availableColumns: List<String>
) : KodivxException(
    "Column '$columnName' not found. Available columns: ${availableColumns.joinToString(", ")}"
)

class SchemaMismatchException(
    message: String
) : KodivxException(message)

class TypeCastException(
    from: String,
    to: String,
    reason: String? = null
) : KodivxException(
    "Cannot cast from $from to $to" + (if (reason != null) ": $reason" else "")
)

class BufferOutOfBoundsException(
    index: Int,
    size: Int
) : KodivxException(
    "Buffer index out of bounds: index $index, size $size"
)
