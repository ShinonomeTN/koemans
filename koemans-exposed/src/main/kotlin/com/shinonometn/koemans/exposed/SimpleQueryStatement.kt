package com.shinonometn.koemans.exposed

import org.intellij.lang.annotations.Language
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.Statement
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import java.sql.ResultSet

class SimpleQueryStatement<T>(@Language("sql") private val sql: String, private val transform: (ResultSet) -> T?) :
    Statement<T>(StatementType.SELECT, emptyList()) {
    override fun prepareSQL(transaction: Transaction) = sql
    override fun arguments(): Iterable<Iterable<Pair<IColumnType, Any?>>> = emptyList()
    override fun PreparedStatementApi.executeInternal(transaction: Transaction): T? {
        return executeQuery().use { transform(it) }
    }
}

/**
 * Execute a query in transaction. It will force the sql be executed as a query statement in Exposed.
 * @param sql Sql expression
 * @param transform ResultRow mapper
 */
fun <T> Transaction.query(@Language("sql") sql: String, transform: (ResultSet) -> T?): T? = exec(SimpleQueryStatement(sql, transform))