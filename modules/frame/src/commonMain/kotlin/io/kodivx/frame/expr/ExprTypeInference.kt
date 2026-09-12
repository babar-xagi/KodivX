package io.kodivx.frame

import io.kodivx.core.error.ColumnNotFoundException
import io.kodivx.core.schema.Schema
import io.kodivx.core.type.DataType

/**
 * Infers the resulting [DataType] of an [Expr] when evaluated against a table [Schema].
 * Throws [IllegalArgumentException] or [ColumnNotFoundException] on type mismatches or missing columns.
 */
fun Expr.inferType(schema: Schema): DataType = when (this) {
    is Expr.Literal -> scalar.type

    is Expr.ColumnRef -> {
        val index = schema.indexOf(name)
        if (index == -1) throw ColumnNotFoundException(name, schema.fieldNames)
        val actualType = schema[index].type
        if (explicitType != null && explicitType != actualType) {
            throw IllegalArgumentException(
                "Column '$name' expected type $explicitType but schema has $actualType"
            )
        }
        actualType
    }

    is Expr.Alias -> expr.inferType(schema)

    is Expr.Cast -> {
        expr.inferType(schema) // Validate inner expr
        targetType
    }

    is Expr.UnaryMinus -> {
        val t = expr.inferType(schema)
        require(t.isNumeric) { "Unary minus requires numeric operand, got: $t in ($outputName)" }
        t
    }

    is Expr.Not -> {
        val t = expr.inferType(schema)
        require(t == DataType.Boolean) { "Operator NOT requires Boolean operand, got: $t in ($outputName)" }
        DataType.Boolean
    }

    is Expr.IsNull -> {
        expr.inferType(schema)
        DataType.Boolean
    }

    is Expr.IsNotNull -> {
        expr.inferType(schema)
        DataType.Boolean
    }

    is Expr.Add -> {
        val l = left.inferType(schema)
        val r = right.inferType(schema)
        if (l == DataType.Utf8 || r == DataType.Utf8) {
            DataType.Utf8
        } else {
            coerceNumeric(l, r, outputName)
        }
    }

    is Expr.Subtract, is Expr.Multiply, is Expr.Divide, is Expr.Modulo -> {
        val l = when (this) {
            is Expr.Subtract -> left.inferType(schema)
            is Expr.Multiply -> left.inferType(schema)
            is Expr.Divide -> left.inferType(schema)
            is Expr.Modulo -> left.inferType(schema)
        }
        val r = when (this) {
            is Expr.Subtract -> right.inferType(schema)
            is Expr.Multiply -> right.inferType(schema)
            is Expr.Divide -> right.inferType(schema)
            is Expr.Modulo -> right.inferType(schema)
        }
        coerceNumeric(l, r, outputName)
    }

    is Expr.Equal, is Expr.NotEqual -> {
        val (l, r) = when (this) {
            is Expr.Equal -> left.inferType(schema) to right.inferType(schema)
            is Expr.NotEqual -> left.inferType(schema) to right.inferType(schema)
        }
        require((l.isNumeric && r.isNumeric) || l == r) {
            "Equality comparison requires comparable types, got: $l and $r in ($outputName)"
        }
        DataType.Boolean
    }

    is Expr.GreaterThan, is Expr.GreaterOrEqual, is Expr.LessThan, is Expr.LessOrEqual -> {
        val (l, r) = when (this) {
            is Expr.GreaterThan -> left.inferType(schema) to right.inferType(schema)
            is Expr.GreaterOrEqual -> left.inferType(schema) to right.inferType(schema)
            is Expr.LessThan -> left.inferType(schema) to right.inferType(schema)
            is Expr.LessOrEqual -> left.inferType(schema) to right.inferType(schema)
        }
        require((l.isNumeric && r.isNumeric) || (l == DataType.Utf8 && r == DataType.Utf8)) {
            "Comparison requires numeric or string operands, got: $l and $r in ($outputName)"
        }
        DataType.Boolean
    }

    is Expr.And, is Expr.Or -> {
        val (l, r) = when (this) {
            is Expr.And -> left.inferType(schema) to right.inferType(schema)
            is Expr.Or -> left.inferType(schema) to right.inferType(schema)
        }
        require(l == DataType.Boolean && r == DataType.Boolean) {
            "Logical operator requires Boolean operands, got: $l and $r in ($outputName)"
        }
        DataType.Boolean
    }

    is Expr.Aggregate -> {
        when (kind) {
            Expr.AggKind.Count -> DataType.Int32
            Expr.AggKind.Sum, Expr.AggKind.Mean, Expr.AggKind.Variance, Expr.AggKind.StdDev -> DataType.Float64
            Expr.AggKind.Min, Expr.AggKind.Max -> {
                val innerType = expr?.inferType(schema) ?: DataType.Float64
                require(innerType.isNumeric || innerType == DataType.Utf8) {
                    "Min/Max requires numeric or string column, got: $innerType"
                }
                innerType
            }
        }
    }

    is Aggregation -> {
        when (this) {
            is Aggregation.Count -> DataType.Int32
            is Aggregation.Min, is Aggregation.Max -> {
                val colName = column ?: error("Column name required for Min/Max aggregation")
                val colType = schema[colName].type
                require(colType.isNumeric || colType == DataType.Utf8) {
                    "Min/Max requires numeric or string column, got: $colType"
                }
                colType
            }
            else -> DataType.Float64
        }
    }

    is Expr.Contains, is Expr.StartsWith, is Expr.EndsWith -> {
        val t = when (this) {
            is Expr.Contains -> expr.inferType(schema)
            is Expr.StartsWith -> expr.inferType(schema)
            is Expr.EndsWith -> expr.inferType(schema)
        }
        require(t == DataType.Utf8) { "String match operation requires Utf8 operand, got: $t in ($outputName)" }
        DataType.Boolean
    }

    is Expr.Lower, is Expr.Upper -> {
        val t = when (this) {
            is Expr.Lower -> expr.inferType(schema)
            is Expr.Upper -> expr.inferType(schema)
        }
        require(t == DataType.Utf8) { "String case conversion requires Utf8 operand, got: $t in ($outputName)" }
        DataType.Utf8
    }

    is Expr.Length -> {
        val t = expr.inferType(schema)
        require(t == DataType.Utf8) { "Length operation requires Utf8 operand, got: $t in ($outputName)" }
        DataType.Int32
    }
}

/**
 * Validates the expression AST against the given [Schema].
 * Throws an exception if any type mismatch or missing column is found.
 */
fun Expr.validate(schema: Schema) {
    inferType(schema)
}

private fun coerceNumeric(left: DataType, right: DataType, context: String): DataType {
    require(left.isNumeric && right.isNumeric) {
        "Arithmetic operation requires numeric operands, got: $left and $right in $context"
    }
    return when {
        left == DataType.Float64 || right == DataType.Float64 -> DataType.Float64
        left == DataType.Float32 || right == DataType.Float32 -> DataType.Float32
        left == DataType.Int64 || right == DataType.Int64 -> DataType.Int64
        else -> DataType.Int32
    }
}
