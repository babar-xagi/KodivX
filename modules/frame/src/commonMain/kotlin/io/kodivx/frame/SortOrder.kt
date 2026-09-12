package io.kodivx.frame

/**
 * Defines sort ordering for a column.
 */
data class SortOrder(
    val columnName: String,
    val ascending: Boolean = true,
    val nullsFirst: Boolean = false
)

fun asc(columnName: String, nullsFirst: Boolean = false): SortOrder =
    SortOrder(columnName, ascending = true, nullsFirst = nullsFirst)

fun desc(columnName: String, nullsFirst: Boolean = false): SortOrder =
    SortOrder(columnName, ascending = false, nullsFirst = nullsFirst)
