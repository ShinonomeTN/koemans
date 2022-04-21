@file:Suppress("FunctionName")

package com.shinonometn.koemans.exposed.datasource

import com.shinonometn.koemans.exposed.database.SqlDatabaseConfiguration
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

@Suppress("unused")
fun SqlDatabaseConfiguration.HikariDatasource(hikariConfigurator: (HikariConfig) -> Unit): SqlDatabaseConfiguration.() -> DataSource = {
    val driverClassName = driverClassName

    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-${databaseTypeName}-${name}"
        this.driverClassName = driverClassName
        jdbcUrl = urlFactory()
    }

    // Apply config overwrite
    hikariConfig.apply(hikariConfigurator)

    HikariDataSource(hikariConfig)
}

/**
 *  Create HikariDatasource with default options:
 *  minimumIdle = 1
 *  maximumPoolSize = 1
 *
 *  So we encourage you to provide a more detailed configuration with `HikariDatasource {}`.
 */
@Suppress("unused")
fun SqlDatabaseConfiguration.HikariDatasource(): SqlDatabaseConfiguration.() -> DataSource = {
    val driverClassName = driverClassName
    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-${databaseTypeName}-${name}"

        this.driverClassName = driverClassName

        jdbcUrl = urlFactory()
        minimumIdle = 1
        maximumPoolSize = 1
    }

    HikariDataSource(hikariConfig)
}