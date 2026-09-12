package io.kodivx.frame

import io.kodivx.buffer.Buffer
import io.kodivx.buffer.primitive.*
import io.kodivx.buffer.selection.SelectionVector
import io.kodivx.buffer.string.Utf8StringBuffer
import io.kodivx.core.error.BufferOutOfBoundsException
import io.kodivx.core.schema.Field
import io.kodivx.core.type.DataType

/**
 * Represents a strongly-typed named column backed by an unboxed physical [Buffer].
 */
interface Column<T> {
    val name: String
    val type: DataType
    val buffer: Buffer
    val size: Int get() = buffer.size
    val isNullable: Boolean get() = buffer.isNullable
    val nullCount: Int get() = buffer.nullCount

    val field: Field get() = Field(name, type, isNullable)

    operator fun get(index: Int): T?

    fun isNull(index: Int): Boolean = buffer.isNull(index)
    fun isValid(index: Int): Boolean = buffer.isValid(index)

    fun slice(offset: Int, length: Int): Column<T>
    fun withName(newName: String): Column<T>
    fun filterWithSelection(selection: SelectionVector): Column<T>
}

class IntColumn(
    override val name: String,
    override val buffer: IntBuffer
) : Column<Int> {
    override val type: DataType get() = DataType.Int32

    fun getInt(index: Int): Int = buffer.getInt(index)

    override fun get(index: Int): Int? = buffer.getBoxed(index)

    override fun slice(offset: Int, length: Int): IntColumn =
        IntColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): IntColumn = IntColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): IntColumn {
        val outData = IntArray(selection.length)
        val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(selection.length)
        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            if (buffer.isNull(srcIdx)) {
                vBuilder.appendNull()
                outData[i] = 0
            } else {
                vBuilder.appendValid()
                outData[i] = buffer.getInt(srcIdx)
            }
        }
        return IntColumn(name, IntBuffer(outData, 0, selection.length, vBuilder.build()))
    }

    override fun toString(): String = "IntColumn($name, size=$size)"
}

class LongColumn(
    override val name: String,
    override val buffer: LongBuffer
) : Column<Long> {
    override val type: DataType get() = DataType.Int64

    fun getLong(index: Int): Long = buffer.getLong(index)

    override fun get(index: Int): Long? = buffer.getBoxed(index)

    override fun slice(offset: Int, length: Int): LongColumn =
        LongColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): LongColumn = LongColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): LongColumn {
        val outData = LongArray(selection.length)
        val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(selection.length)
        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            if (buffer.isNull(srcIdx)) {
                vBuilder.appendNull()
                outData[i] = 0L
            } else {
                vBuilder.appendValid()
                outData[i] = buffer.getLong(srcIdx)
            }
        }
        return LongColumn(name, LongBuffer(outData, 0, selection.length, vBuilder.build()))
    }

    override fun toString(): String = "LongColumn($name, size=$size)"
}

class DoubleColumn(
    override val name: String,
    override val buffer: DoubleBuffer
) : Column<Double> {
    override val type: DataType get() = DataType.Float64

    fun getDouble(index: Int): Double = buffer.getDouble(index)

    override fun get(index: Int): Double? = buffer.getBoxed(index)

    override fun slice(offset: Int, length: Int): DoubleColumn =
        DoubleColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): DoubleColumn = DoubleColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): DoubleColumn {
        val outData = DoubleArray(selection.length)
        val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(selection.length)
        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            if (buffer.isNull(srcIdx)) {
                vBuilder.appendNull()
                outData[i] = 0.0
            } else {
                vBuilder.appendValid()
                outData[i] = buffer.getDouble(srcIdx)
            }
        }
        return DoubleColumn(name, DoubleBuffer(outData, 0, selection.length, vBuilder.build()))
    }

    override fun toString(): String = "DoubleColumn($name, size=$size)"
}

class FloatColumn(
    override val name: String,
    override val buffer: FloatBuffer
) : Column<Float> {
    override val type: DataType get() = DataType.Float32

    fun getFloat(index: Int): Float = buffer.getFloat(index)

    override fun get(index: Int): Float? = buffer.getBoxed(index)

    override fun slice(offset: Int, length: Int): FloatColumn =
        FloatColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): FloatColumn = FloatColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): FloatColumn {
        val outData = FloatArray(selection.length)
        val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(selection.length)
        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            if (buffer.isNull(srcIdx)) {
                vBuilder.appendNull()
                outData[i] = 0.0f
            } else {
                vBuilder.appendValid()
                outData[i] = buffer.getFloat(srcIdx)
            }
        }
        return FloatColumn(name, FloatBuffer(outData, 0, selection.length, vBuilder.build()))
    }

    override fun toString(): String = "FloatColumn($name, size=$size)"
}

class BooleanColumn(
    override val name: String,
    override val buffer: BooleanBuffer
) : Column<Boolean> {
    override val type: DataType get() = DataType.Boolean

    fun getBoolean(index: Int): Boolean = buffer.getBoolean(index)

    override fun get(index: Int): Boolean? = buffer.getBoxed(index)

    override fun slice(offset: Int, length: Int): BooleanColumn =
        BooleanColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): BooleanColumn = BooleanColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): BooleanColumn {
        val wordCount = (selection.length + 63) ushr 6
        val words = LongArray(wordCount)
        val vBuilder = io.kodivx.buffer.bitmap.ValidityBitmapBuilder(selection.length)

        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            if (buffer.isNull(srcIdx)) {
                vBuilder.appendNull()
            } else {
                vBuilder.appendValid()
                if (buffer.getBoolean(srcIdx)) {
                    val wIdx = i ushr 6
                    val bIdx = i and 63
                    words[wIdx] = words[wIdx] or (1L shl bIdx)
                }
            }
        }
        return BooleanColumn(name, BooleanBuffer(words, 0, selection.length, vBuilder.build()))
    }

    override fun toString(): String = "BooleanColumn($name, size=$size)"
}

class StringColumn(
    override val name: String,
    override val buffer: Utf8StringBuffer
) : Column<String> {
    override val type: DataType get() = DataType.Utf8

    fun getString(index: Int): String? = buffer.getString(index)

    override fun get(index: Int): String? = buffer.getString(index)

    override fun slice(offset: Int, length: Int): StringColumn =
        StringColumn(name, buffer.slice(offset, length))

    override fun withName(newName: String): StringColumn = StringColumn(newName, buffer)

    override fun filterWithSelection(selection: SelectionVector): StringColumn {
        val builder = io.kodivx.buffer.builder.Utf8StringBufferBuilder(selection.length)
        for (i in 0 until selection.length) {
            val srcIdx = selection[i]
            val str = buffer.getString(srcIdx)
            if (str != null) builder.append(str) else builder.appendNull()
        }
        return StringColumn(name, builder.build())
    }

    override fun toString(): String = "StringColumn($name, size=$size)"
}
