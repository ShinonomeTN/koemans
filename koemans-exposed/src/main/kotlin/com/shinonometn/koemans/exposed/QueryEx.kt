package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.*

/**
 * Create Exposed sql expression
 */
@Suppress("FunctionName")
@Deprecated(
    "Deprecated, use direct sql builder instead",
    ReplaceWith("with(SqlExpressionBuilder) {}", "org.jetbrains.exposed.sql.SqlExpressionBuilder")
)
fun ESQL(builder : SqlExpressionBuilder.() -> Op<Boolean>) = SqlExpressionBuilder.builder()

/** Count by a column */
fun <R, T : Table> T.countBy(column: Column<R>, predicate : (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : Long {
    val colCount = column.count()
    return slice(colCount).let { fs ->
        predicate?.let { fs.select { predicate(this@countBy) } } ?: fs.selectAll()
    }.map { it[colCount] }.first()
}

/** Sum by a column */
fun <R, T : Table> T.sumBy(column: Column<R>, predicate: (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : R? {
    val colSum = column.sum()
    return slice(colSum).let { fs ->
        predicate?.let { fs.select { predicate(this@sumBy) } } ?: fs.selectAll()
    }.map { it[colSum] }.firstOrNull()
}