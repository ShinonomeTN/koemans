package com.shinonometn.koemans.exposed.database

import com.shinonometn.koemans.utils.MutableUrlParameters
import com.shinonometn.koemans.utils.mutableUrlParametersOf
import com.shinonometn.koemans.utils.urlEncoded
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.statements.StatementContext
import org.jetbrains.exposed.sql.statements.expandArgs
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javax.sql.DataSource
import kotlin.properties.Delegates

class Sqlite3 internal constructor(
    val name: String,
    override val db: Database,
    override val datasource: DataSource?
) : SqlDatabase {

    class Configuration(override var driverClassName: String = "org.sqlite.JDBC") : SqlDatabaseConfiguration() {

        override val databaseTypeName = "Sqlite3"

        var logger = LoggerFactory.getLogger("Sqlite3")

        override fun exposed(builder: DatabaseConfig.Builder.() -> Unit) {
            exposedDatabaseConfigFactory = {
                DatabaseConfig {
                    sqlLogger = object : SqlLogger {
                        override fun log(context: StatementContext, transaction: Transaction) {
                            logger.debug(context.expandArgs(TransactionManager.current()))
                        }

                    }

                    builder()
                }
            }
        }

        override var name: String by Delegates.notNull()
        private val namePattern = Regex("^[A-Za-z0-9_\\-.]+$")

        override var urlFactory: () -> String = { error("Please specific a sqlite3 persistent mode via dsl like 'inFile { ... }'") }

        override var dataSource: SqlDatabaseConfiguration.() -> DataSource? = { null }

        override var defaultTransactionLevel: TransactionLevel? = TransactionLevel.STRICT

        private var urlParams: MutableUrlParameters? = null

        /**
         * Set url parameters after connection url
         */
        fun parameters(builder: MutableUrlParameters.() -> Unit) {
            val params = urlParams ?: mutableUrlParametersOf()
            params.builder()
            urlParams = params
        }

        @Deprecated("Use 'dataSource' instead")
        var poolingStrategy: Configuration.() -> Database = DefaultNoPool

        /**
         * Use a file as sqlite3 database, with given name in specified directory.
         * It will add `.db` after the [name] as the filename.
         *
         * Currently, the name should match pattern '`^[A-Za-z0-9_\-.]+$`'.
         *
         * if [mkdirs] is true, [directory] will be created if not exists.
         */
        fun inFile(name: String, directory: String, mkdirs: Boolean = false) {
            inFile(name, Paths.get(directory), mkdirs)
        }

        /**
         * Use a file as sqlite3 database, with given name in specified directory.
         * It will add `.db` after the [name] as the filename.
         *
         * Currently, the name should match pattern '`^[A-Za-z0-9_\-.]+$`'.
         *
         * if [mkdirs] is true, [directory] will be created if not exists.
         */
        fun inFile(name: String, directory: Path, mkdirs: Boolean = false) {
            require(name.matches(namePattern)) { "illegal_name_pattern:$name" }
            this.name = name

            inFile(directory.resolve("$name.db").toAbsolutePath().toString(), mkdirs)
        }

        /**
         * Use a file as sqlite3 database.
         * The filename without extension will be the name of this database.
         *
         * if [mkdirs] is true, parent directory will be created if not exists.
         */
        fun inFile(filePath: String, mkdirs: Boolean = false) {
            val file = File(filePath)
            val parentFile = file.parentFile
            if (!parentFile.exists() && mkdirs && !parentFile.mkdirs()) {
                error("Could not create folder '${parentFile.absoluteFile}' for database file '${file.name}'.")
            }
            inFile(file)
        }

        /**
         * Use a file as sqlite3 database
         * The filename without extension will be the name of this database
         */
        fun inFile(file: File) {
            this.name = file.nameWithoutExtension
            urlFactory = {
                val url = "jdbc:sqlite:${file.absoluteFile}"
                val urlParams = this.urlParams
                if (!urlParams.isNullOrEmpty()) "$url?${urlParams.urlEncoded()}" else url
            }
        }

        /**
         * Use in memory sqlite3 database, force set `mode=memory` in url query params
         * If no parameter specified, `cache=shared` option will be added
         */
        fun inMemory(name: String) {
            require(name.matches(namePattern)) { "illegal_name_pattern:$name" }
            this.name = name
            urlFactory = {
                val url = "jdbc:sqlite:file:${name}?mode=memory"
                val urlParams = this.urlParams
                if (!urlParams.isNullOrEmpty()) "$url&${urlParams.urlEncoded()}" else "$url&cache=shared"
            }
        }

        companion object {
            @Deprecated("Use 'dataSource = { null }' instead")
            val Configuration.DefaultNoPool by lazy<Configuration.() -> Database> {
                { Database.connect(urlFactory(), driverClassName) }
            }
        }
    }

    companion object : SqlDatabaseType<Sqlite3, Configuration>({ Configuration() }) {
        override fun createNewDatabase(config: Configuration, database: Database, dataSource: DataSource?): Sqlite3 {
            config.logger.info("Created sqlite3 database connection '{}'.", config.name)
            return Sqlite3(config.name, database, dataSource)
        }
    }
}