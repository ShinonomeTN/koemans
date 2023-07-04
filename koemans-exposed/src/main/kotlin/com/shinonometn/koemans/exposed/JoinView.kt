package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.*

/**
 * JoinView is a database-view like Join expression cache.
 */
abstract class JoinView {

    abstract val source: ColumnSet

    val columns : List<Column<*>>
        get() = _columns
    private val _columns = ArrayList<Column<*>>()

    protected fun <T> referringColumn(column : Column<T>) : Column<T> {
        if(!_columns.contains(column)) _columns.add(column)
        return column
    }

    protected fun referringColumns(columnSet: ColumnSet) {
        columnSet.columns.forEach { if(it !in _columns) _columns.add(it) }
    }
}

fun JoinView.selectBy(
    filterRequest: FilterRequest,
    additionalBuilder: (SqlExpressionBuilder.(Op<Boolean>) -> Op<Boolean>)? = null
) = source.selectBy(filterRequest, additionalBuilder)

fun JoinView.slice(column: Expression<*>, vararg columns: Expression<*>): FieldSet = Slice(source, listOf(column) + columns)

fun JoinView.slice(columns: List<Expression<*>>): FieldSet = Slice(source, columns)

fun JoinView.select(where: SqlExpressionBuilder.() -> Op<Boolean>): Query = source.select(SqlExpressionBuilder.where())