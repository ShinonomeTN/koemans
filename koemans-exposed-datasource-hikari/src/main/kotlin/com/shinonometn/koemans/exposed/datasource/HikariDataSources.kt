@file:Suppress("FunctionName")

package com.shinonometn.koemans.exposed.datasource

import com.shinonometn.koemans.exposed.database.SqlDatabaseConfiguration
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

/**
 * Configure Hikari Datasource
 *
 * default pool name is 'Hikari-{databaseTypeName}-{databaseName}'
 * `driverClassName`, `jdbcUrl`, `username` and `password` are automatically set.
 */
@Suppress("unused")
fun SqlDatabaseConfiguration.HikariDatasource(hikariConfigurator: HikariConfig.() -> Unit): SqlDatabaseConfiguration.() -> DataSource = {
    val driverClassName = driverClassName
    val username = username ?: ""
    val password = password ?: ""

    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-${databaseTypeName}-${name}"
        this.driverClassName = driverClassName
        jdbcUrl = urlFactory()
        this.username = username
        this.password = password
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
 *
 *  default pool name is 'Hikari-{databaseTypeName}-{databaseName}'
 *  `driverClassName`, `jdbcUrl`, `username` and `password` are automatically set.
 */
@Suppress("unused")
fun SqlDatabaseConfiguration.HikariDatasource(): SqlDatabaseConfiguration.() -> DataSource = {
    val driverClassName = driverClassName
    val username = username ?: ""
    val password = password ?: ""

    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-${databaseTypeName}-${name}"

        this.driverClassName = driverClassName

        this.username = username
        this.password = password

        jdbcUrl = urlFactory()
        minimumIdle = 1
        maximumPoolSize = 1
    }

    HikariDataSource(hikariConfig)
}