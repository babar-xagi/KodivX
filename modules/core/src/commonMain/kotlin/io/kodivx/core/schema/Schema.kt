package io.kodivx.core.schema

import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.type.DataType

/**
 * Represents the ordered column structure and metadata of a DataFrame or analytical dataset.
 */
class Schema(
    val fields: List<Field>
) {
    private val nameToIndex: Map<String, Int> = buildMap {
        fields.forEachIndexed { index, field ->
            require(!containsKey(field.name)) {
                "Duplicate column name '${field.name}' in schema"
            }
            put(field.name, index)
        }
    }

    val size: Int get() = fields.size
    val fieldNames: List<String> get() = fields.map { it.name }

    operator fun get(index: Int): Field = fields[index]

    operator fun get(name: String): Field {
        val index = nameToIndex[name]
            ?: throw ColumnNotFoundException(name, fieldNames)
        return fields[index]
    }

    fun indexOf(name: String): Int = nameToIndex[name] ?: -1

    fun contains(name: String): Boolean = nameToIndex.containsKey(name)

    fun withField(field: Field): Schema {
        require(!contains(field.name)) {
            "Schema already contains a column named '${field.name}'"
        }
        return Schema(fields + field)
    }

    fun dropField(name: String): Schema {
        if (!contains(name)) throw ColumnNotFoundException(name, fieldNames)
        return Schema(fields.filterNot { it.name == name })
    }

    fun select(vararg names: String): Schema {
        val selectedFields = names.map { name ->
            this[name]
        }
        return Schema(selectedFields)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Schema) return false
        return fields == other.fields
    }

    override fun hashCode(): Int = fields.hashCode()

    override fun toString(): String = buildString {
        append("Schema(")
        append(fields.joinToString(", "))
        append(")")
    }

    companion object {
        val EMPTY = Schema(emptyList())

        fun of(vararg fields: Field): Schema = Schema(fields.toList())
    }
}

/**
 * Builder DSL for creating [Schema] instances.
 */
class SchemaBuilder {
    private val fields = mutableListOf<Field>()

    fun field(name: String, type: DataType, nullable: Boolean = true, metadata: Map<String, String> = emptyMap()) {
        fields.add(Field(name, type, nullable, metadata))
    }

    fun boolean(name: String, nullable: Boolean = true) = field(name, DataType.Boolean, nullable)
    fun int(name: String, nullable: Boolean = true) = field(name, DataType.Int32, nullable)
    fun long(name: String, nullable: Boolean = true) = field(name, DataType.Int64, nullable)
    fun float(name: String, nullable: Boolean = true) = field(name, DataType.Float32, nullable)
    fun double(name: String, nullable: Boolean = true) = field(name, DataType.Float64, nullable)
    fun string(name: String, nullable: Boolean = true) = field(name, DataType.Utf8, nullable)
    fun binary(name: String, nullable: Boolean = true) = field(name, DataType.Binary, nullable)

    fun build(): Schema = Schema(fields.toList())
}

/**
 * DSL entrypoint for building a [Schema].
 */
inline fun schema(init: SchemaBuilder.() -> Unit): Schema {
    return SchemaBuilder().apply(init).build()
}
