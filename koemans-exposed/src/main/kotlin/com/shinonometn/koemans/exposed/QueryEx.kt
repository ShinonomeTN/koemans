package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.*

fun <R, T : Table> T.countBy(column: Column<R>, predicate : (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : Long {
    return slice(column.count()).let { fs ->
        predicate?.let { fs.select { predicate(this@countBy) } } ?: fs.selectAll()
    }.map { it[column.count()] }.first()
}

fun <R, T : Table> T.sumBy(column: Column<R>, predicate: (SqlExpressionBuilder.(T) -> Op<Boolean>)? = null) : R? {
    return slice(column.sum()).let { fs ->
        predicate?.let { fs.select { predicate(this@sumBy) } } ?: fs.selectAll()
    }.map { it[column.sum()] }.firstOrNull()
}