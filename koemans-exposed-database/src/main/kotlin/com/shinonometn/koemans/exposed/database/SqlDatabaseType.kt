package com.shinonometn.koemans.exposed.database

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import javax.sql.DataSource

abstract class SqlDatabaseType<TDatabase : SqlDatabase, TConfiguration : SqlDatabaseConfiguration>(
    private val configurationProvider : () -> TConfiguration
) {

    open fun createDatabase(block : TConfiguration.() -> Unit) : TDatabase {
        val config = configurationProvider().apply(block)
        val datasource = config.dataSource(config)
        val database = if(datasource == null) Database.connect(config.urlFactory(), config.driverClassName)
            else Database.connect(datasource)
        config.defaultTransactionLevel?.let { database.transactionManager.defaultIsolationLevel = it.level }

        return createNewDatabase(config, database, datasource)
    }

    protected abstract fun createNewDatabase(config : TConfiguration, database : Database, dataSource: DataSource?) : TDatabase
}