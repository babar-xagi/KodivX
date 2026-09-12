package io.kodivx.core.schema

import io.kodivx.core.type.DataType

/**
 * Represents a single column definition within a [Schema].
 */
data class Field(
    val name: String,
    val type: DataType,
    val nullable: Boolean = true,
    val metadata: Map<String, String> = emptyMap()
) {
    init {
        require(name.isNotBlank()) { "Field name cannot be blank" }
    }

    override fun toString(): String = buildString {
        append(name)
        append(": ")
        append(type.displayName)
        if (nullable) append("?")
    }
}
