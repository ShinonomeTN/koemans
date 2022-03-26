package com.shinonometn.koemans.exposed.database.sqlite

import com.shinonometn.koemans.exposed.database.SqlDatabase
import com.shinonometn.koemans.exposed.database.SqlDatabaseType
import com.shinonometn.koemans.exposed.database.TransactionLevel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transactionManager
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import javax.sql.DataSource
import kotlin.properties.Delegates

class Sqlite3 internal constructor(val name: String, override val db: Database, val datasource : DataSource?) : SqlDatabase {

    class Configuration {
        internal var name: String by Delegates.notNull()
        private val namePattern = Regex("^[A-Za-z0-9_\\-.]+$")

        internal var urlFactory: () -> String = { error("Please specific a sqlite3 persistent mode via dsl like 'inFile { ... }'") }

        var dataSource: Configuration.(url : String) -> DataSource? = { null }
        var transactionLevel: TransactionLevel = TransactionLevel.STRICT

        @Deprecated("Use 'dataSource' instead")
        var poolingStrategy : Configuration.() -> Database = DefaultNoPool

        fun inFile(name: String, storageLocation: Path, mkdirs: Boolean = false) {
            require(name.matches(namePattern)) { "illegal_name_pattern:$name" }
            this.name = name

            val dir = File(storageLocation.toUri())

            if (!dir.exists()) {
                if (mkdirs && !dir.mkdirs()) error("Could not create folder '$storageLocation' for database '$name'.")
                else error("Folder '${dir.absolutePath}' does not exists.")
            }

            urlFactory = { "jdbc:sqlite:${storageLocation.resolve("$name.db")}" }
        }

        fun inMemory(name: String) {
            require(name.matches(namePattern)) { "illegal_name_pattern:$name" }
            this.name = name
            urlFactory = { "jdbc:sqlite:file:${name}?mode=memory&cache=shared" }
        }

        companion object {
            @Deprecated("Use 'dataSource = { null }' instead")
            val Configuration.DefaultNoPool by lazy<Configuration.() -> Database> {
                { Database.connect(urlFactory(), DriverClassName) }
            }
        }
    }

    companion object : SqlDatabaseType<Sqlite3, Configuration> {
        private val logger = LoggerFactory.getLogger("Sqlite3")

        const val DriverClassName = "org.sqlite.JDBC"

        override fun createDatabase(block: Configuration.() -> Unit): Sqlite3 {
            val config = Configuration().apply(block)

            val url = config.urlFactory()
            val datasource = config.dataSource(config,url)

            val database = if(datasource == null) config.poolingStrategy(config) else Database.connect(datasource)
            database.transactionManager.defaultIsolationLevel = config.transactionLevel.level

            logger.info("Created sqlite3 database connection '{}'.", config.name)

            return Sqlite3(config.name, database, datasource)
        }
    }
}