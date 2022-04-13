@file:Suppress("FunctionName")

package com.shinonometn.koemans.exposed.database.sqlite

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import javax.sql.DataSource

@Deprecated("Use 'HikariDatasource {}' instead")
fun Sqlite3.Configuration.HikariPooling(hikariConfigurator : (HikariConfig) -> Unit) : Sqlite3.Configuration.() -> Database {
    val hikariConfig = HikariConfig().apply(hikariConfigurator)

    hikariConfig.jdbcUrl = urlFactory()
    hikariConfig.driverClassName = Sqlite3.DriverClassName

    return { Database.connect(HikariDataSource(hikariConfig)) }
}

@Suppress("unused")
@Deprecated("Use 'HikariDatasource()' instead")
fun Sqlite3.Configuration.HikariPooling() : Sqlite3.Configuration.() -> Database = {
    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-Sqlite3-${name}"
        driverClassName = Sqlite3.DriverClassName
        jdbcUrl = urlFactory()
        minimumIdle = 1
        maximumPoolSize = 1
    }

    Database.connect(HikariDataSource(hikariConfig))
}

@Deprecated("Use HikariPooling() instead")
val Sqlite3.Configuration.HikariPooling by lazy<Sqlite3.Configuration.() -> Database> {
    { HikariPooling().invoke(this) }
}

@Suppress("unused")
fun Sqlite3.Configuration.HikariDatasource(hikariConfigurator: (HikariConfig) -> Unit) : Sqlite3.Configuration.(String) -> DataSource = {
    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-Sqlite3-${name}"
        driverClassName = Sqlite3.DriverClassName
        jdbcUrl = it
        minimumIdle = 1
        maximumPoolSize = 1
    }

    // Apply config overwrite
    hikariConfig.apply(hikariConfigurator)

    HikariDataSource(hikariConfig)
}

@Suppress("unused")
fun Sqlite3.Configuration.HikariDatasource() : Sqlite3.Configuration.(String) -> DataSource = {
    val hikariConfig = HikariConfig().apply {
        poolName = "Hikari-Sqlite3-${name}"
        driverClassName = Sqlite3.DriverClassName
        jdbcUrl = it
        minimumIdle = 1
        maximumPoolSize = 1
    }

    HikariDataSource(hikariConfig)
}