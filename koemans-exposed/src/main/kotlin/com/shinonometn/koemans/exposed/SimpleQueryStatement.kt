package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import java.sql.ResultSet

class SimpleQueryStatement<T>(private val sql : String, private val transform : (ResultSet) -> T?) : Statement<T>(StatementType.SELECT, emptyList()) {
    override fun prepareSQL(transaction: Transaction) = sql
    override fun arguments(): Iterable<Iterable<Pair<IColumnType, Any?>>> = emptyList()
    override fun PreparedStatementApi.executeInternal(transaction: Transaction) : T? {
        return executeQuery().use { transform(it) }
    }
}

fun <T> Transaction.query(sql : String, transform : (ResultSet) -> T?) : T? = exec(SimpleQueryStatement(sql, transform))