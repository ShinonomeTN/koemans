package com.shinonometn.koemans.exposed.database.sqlite

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

fun Sqlite3.Configuration.HikariPooling(config : (HikariConfig) -> Unit) : Sqlite3.Configuration.() -> Database {
    val hikariConfig = HikariConfig().apply(config)

    hikariConfig.jdbcUrl = urlFactory()
    hikariConfig.driverClassName = Sqlite3.DriverClassName

    return { Database.connect(HikariDataSource(hikariConfig)) }
}

val Sqlite3.Configuration.HikariPooling by lazy<Sqlite3.Configuration.() -> Database> {
    {
        val hikariConfig = HikariConfig().apply {
            poolName = "Hikari-Sqlite3-${name}"
            driverClassName = Sqlite3.DriverClassName
            jdbcUrl = urlFactory()
            minimumIdle = 1
            maximumPoolSize = 1
        }

        Database.connect(HikariDataSource(hikariConfig))
    }
}