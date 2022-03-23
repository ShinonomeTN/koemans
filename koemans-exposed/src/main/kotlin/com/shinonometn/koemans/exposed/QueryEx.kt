package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.*

fun <R, T : Table> T.countBy(column: Column<R>, predicate : (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : Long {
    val colCount = column.count()
    return slice(colCount).let { fs ->
        predicate?.let { fs.select { predicate(this@countBy) } } ?: fs.selectAll()
    }.map { it[colCount] }.first()
}

fun <R, T : Table> T.sumBy(column: Column<R>, predicate: (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : R? {
    val colSum = column.sum()
    return slice(colSum).let { fs ->
        predicate?.let { fs.select { predicate(this@sumBy) } } ?: fs.selectAll()
    }.map { it[colSum] }.firstOrNull()
}