package com.shinonometn.koemans.exposed.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.transactionManager
import javax.sql.DataSource

typealias DataSourceProvider = SqlDatabaseConfiguration.() -> DataSource?

interface SqlDatabase {
    /** The exact Exposed Database */
    val db : Database

    /** The datasource this Exposed Database currently used. `null` if no datasource. */
    val datasource : DataSource?

    operator fun <T> invoke(transactionLevel: TransactionLevel? = null, statement: Transaction.() -> T) : T = transaction(
        transactionLevel?.level ?: db.transactionManager.defaultIsolationLevel,
        db.transactionManager.defaultRepetitionAttempts,
        db
    ) {
        statement()
    }
}

/**
 * Create a SQL Database connection with specified [databaseType].
 *
 * After that, you can use it for database operations
 * ```kotlin
 * val database = sqlDatabase(Sqlite3) {
 *      // do your configuration here
 * }
 *
 * database {
 *      // do your database operations here
 * }
 * ```
 */
fun <TDatabase : SqlDatabase, TConfiguration : SqlDatabaseConfiguration> sqlDatabase(
    databaseType : SqlDatabaseType<TDatabase, TConfiguration>,
    config : TConfiguration.() -> Unit
) : TDatabase = databaseType.createDatabase(config)