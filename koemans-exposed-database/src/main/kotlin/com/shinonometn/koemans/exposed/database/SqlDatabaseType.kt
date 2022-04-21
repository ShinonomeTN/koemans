package com.shinonometn.koemans.exposed.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import javax.sql.DataSource

abstract class SqlDatabaseType<TDatabase : SqlDatabase, TConfiguration : SqlDatabaseConfiguration>(
    private val configurationProvider: () -> TConfiguration
) {

    open fun createDatabase(block: TConfiguration.() -> Unit): TDatabase {
        val config = configurationProvider().apply(block)
        val datasource = config.dataSource(config)
        // if datasource is null, use Exposed's default connection
        val database = if (datasource == null) {
            val url = config.urlFactory()
            // If the config said the database type support username password credential, then we will use it.
            if (config.supportUsernamePassword) Database.connect(url, config.driverClassName, config.username ?: "", config.password ?: "")
            // Else return a default connection with empty username and password
            else Database.connect(url, config.driverClassName)
        } else Database.connect(datasource) // If has datasource, use datasource to create connection

        // If config has default transaction level
        config.defaultTransactionLevel?.let { database.transactionManager.defaultIsolationLevel = it.level }

        return createNewDatabase(config, database, datasource)
    }

    protected abstract fun createNewDatabase(config: TConfiguration, database: Database, dataSource: DataSource?): TDatabase
}