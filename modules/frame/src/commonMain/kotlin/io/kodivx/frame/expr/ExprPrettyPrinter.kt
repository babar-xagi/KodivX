package io.kodivx.frame

/**
 * Returns a human-readable mathematical / SQL-like string representation of the [Expr].
 */
fun Expr.toPrettyString(): String = when (this) {
    is Expr.Literal -> scalar.toString()
    is Expr.ColumnRef -> "col(\"$name\")"
    is Expr.Alias -> "(${expr.toPrettyString()} AS \"$aliasName\")"
    is Expr.Cast -> "CAST(${expr.toPrettyString()} AS ${targetType.displayName})"
    is Expr.UnaryMinus -> "(-${expr.toPrettyString()})"
    is Expr.Not -> "NOT (${expr.toPrettyString()})"
    is Expr.IsNull -> "(${expr.toPrettyString()} IS NULL)"
    is Expr.IsNotNull -> "(${expr.toPrettyString()} IS NOT NULL)"
    is Expr.Add -> "(${left.toPrettyString()} + ${right.toPrettyString()})"
    is Expr.Subtract -> "(${left.toPrettyString()} - ${right.toPrettyString()})"
    is Expr.Multiply -> "(${left.toPrettyString()} * ${right.toPrettyString()})"
    is Expr.Divide -> "(${left.toPrettyString()} / ${right.toPrettyString()})"
    is Expr.Modulo -> "(${left.toPrettyString()} % ${right.toPrettyString()})"
    is Expr.Equal -> "(${left.toPrettyString()} == ${right.toPrettyString()})"
    is Expr.NotEqual -> "(${left.toPrettyString()} != ${right.toPrettyString()})"
    is Expr.GreaterThan -> "(${left.toPrettyString()} > ${right.toPrettyString()})"
    is Expr.GreaterOrEqual -> "(${left.toPrettyString()} >= ${right.toPrettyString()})"
    is Expr.LessThan -> "(${left.toPrettyString()} < ${right.toPrettyString()})"
    is Expr.LessOrEqual -> "(${left.toPrettyString()} <= ${right.toPrettyString()})"
    is Expr.And -> "(${left.toPrettyString()} AND ${right.toPrettyString()})"
    is Expr.Or -> "(${left.toPrettyString()} OR ${right.toPrettyString()})"
    is Expr.Aggregate -> "${kind.name.uppercase()}(${expr?.toPrettyString() ?: "*"})"
    is Aggregation -> outputName
    is Expr.Contains -> "CONTAINS(${expr.toPrettyString()}, \"$pattern\")"
    is Expr.StartsWith -> "STARTS_WITH(${expr.toPrettyString()}, \"$prefix\")"
    is Expr.EndsWith -> "ENDS_WITH(${expr.toPrettyString()}, \"$suffix\")"
    is Expr.Lower -> "LOWER(${expr.toPrettyString()})"
    is Expr.Upper -> "UPPER(${expr.toPrettyString()})"
    is Expr.Length -> "LENGTH(${expr.toPrettyString()})"
}

/**
 * Returns a hierarchical ASCII/Unicode tree diagram of the expression AST.
 */
fun Expr.toTreeString(): String = buildString {
    appendTree(this@toTreeString, "", true)
}.trimEnd()

private fun StringBuilder.appendTree(expr: Expr, indent: String, isLast: Boolean) {
    val branch = if (indent.isEmpty()) "" else if (isLast) "└── " else "├── "
    val childIndent = if (indent.isEmpty()) "" else if (isLast) "$indent    " else "$indent│   "

    when (expr) {
        is Expr.Literal -> {
            append("$indent${branch}Literal(${expr.scalar})\n")
        }
        is Expr.ColumnRef -> {
            val typeStr = if (expr.explicitType != null) ": ${expr.explicitType}" else ""
            append("$indent${branch}Column(${expr.name}$typeStr)\n")
        }
        is Expr.Alias -> {
            append("$indent${branch}Alias(${expr.aliasName})\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Cast -> {
            append("$indent${branch}Cast(to: ${expr.targetType.displayName})\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.UnaryMinus -> {
            append("$indent${branch}UnaryMinus\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Not -> {
            append("$indent${branch}Not\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.IsNull -> {
            append("$indent${branch}IsNull\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.IsNotNull -> {
            append("$indent${branch}IsNotNull\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Add, is Expr.Subtract, is Expr.Multiply, is Expr.Divide, is Expr.Modulo,
        is Expr.Equal, is Expr.NotEqual, is Expr.GreaterThan, is Expr.GreaterOrEqual,
        is Expr.LessThan, is Expr.LessOrEqual, is Expr.And, is Expr.Or -> {
            val (nodeName, left, right) = when (expr) {
                is Expr.Add -> Triple("Add", expr.left, expr.right)
                is Expr.Subtract -> Triple("Subtract", expr.left, expr.right)
                is Expr.Multiply -> Triple("Multiply", expr.left, expr.right)
                is Expr.Divide -> Triple("Divide", expr.left, expr.right)
                is Expr.Modulo -> Triple("Modulo", expr.left, expr.right)
                is Expr.Equal -> Triple("Equal", expr.left, expr.right)
                is Expr.NotEqual -> Triple("NotEqual", expr.left, expr.right)
                is Expr.GreaterThan -> Triple("GreaterThan", expr.left, expr.right)
                is Expr.GreaterOrEqual -> Triple("GreaterOrEqual", expr.left, expr.right)
                is Expr.LessThan -> Triple("LessThan", expr.left, expr.right)
                is Expr.LessOrEqual -> Triple("LessOrEqual", expr.left, expr.right)
                is Expr.And -> Triple("And", expr.left, expr.right)
                is Expr.Or -> Triple("Or", expr.left, expr.right)
            }
            append("$indent$branch$nodeName\n")
            appendTree(left, childIndent, false)
            appendTree(right, childIndent, true)
        }
        is Expr.Aggregate -> {
            append("$indent${branch}Aggregate(${expr.kind.name})\n")
            if (expr.expr != null) {
                appendTree(expr.expr, childIndent, true)
            }
        }
        is Aggregation -> {
            append("$indent${branch}Aggregation(${expr.outputName})\n")
        }
        is Expr.Contains -> {
            append("$indent${branch}Contains(\"${expr.pattern}\")\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.StartsWith -> {
            append("$indent${branch}StartsWith(\"${expr.prefix}\")\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.EndsWith -> {
            append("$indent${branch}EndsWith(\"${expr.suffix}\")\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Lower -> {
            append("$indent${branch}Lower\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Upper -> {
            append("$indent${branch}Upper\n")
            appendTree(expr.expr, childIndent, true)
        }
        is Expr.Length -> {
            append("$indent${branch}Length\n")
            appendTree(expr.expr, childIndent, true)
        }
    }
}
