package com.shinonometn.koemans.exposed.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import java.sql.Connection

enum class TransactionLevel(val level: Int) {
    NONE(Connection.TRANSACTION_NONE),
    ALLOW_UNCOMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    STRICT(Connection.TRANSACTION_SERIALIZABLE)
}

interface SqlDatabase {
    val db : Database

    operator fun <T> invoke(transactionLevel: TransactionLevel? = null, statement: Transaction.() -> T) : T = transaction(
        transactionLevel?.level ?: db.transactionManager.defaultIsolationLevel,
        db.transactionManager.defaultRepetitionAttempts,
        db
    ) {
        statement()
    }
}

interface SqlDatabaseType<TDatabase : SqlDatabase, TConfiguration> {
    fun createDatabase(block : TConfiguration.() -> Unit) : TDatabase
}

fun <TDatabase : SqlDatabase, TConfiguration> sqlDatabase(
    databaseType : SqlDatabaseType<TDatabase, TConfiguration>,
    config : TConfiguration.() -> Unit
) : TDatabase = databaseType.createDatabase(config)