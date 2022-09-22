package com.shinonometn.koemans.exposed.database

import org.jetbrains.exposed.sql.DatabaseConfig
import javax.sql.DataSource

abstract class SqlDatabaseConfiguration {

    /** Optional Database Type name for show. */
    open val databaseTypeName : String = "SQL"

    /**
     * The name of this database/database connection.
     * The usage of this property depends on the implementation.
     * Normally it is just a meta info for convince.
     */
    abstract var name : String
        protected set

    /** Did the database support username password authentication */
    open val supportUsernamePassword : Boolean = false

    /** The username for database authentication. */
    open var username : String? = null
        protected set

    /** The password for database authentication. */
    open var password : String? = null
        protected set

    /** The datasource factory method. Provide a `null` if you don't use datasource */
    open var dataSource: DataSourceProvider = { null }

    /** The driver class name associated to this type of database */
    abstract var driverClassName : String
        protected set

    /** The url factory */
    abstract var urlFactory: () -> String
        protected set

    /** The default transaction level */
    open var defaultTransactionLevel: TransactionLevel? = null
        protected set

    /** Configuration builder for Exposed settings */
    open var exposedDatabaseConfigFactory : () -> DatabaseConfig = { DatabaseConfig() }
        protected set

    /** Exposed setting builder dsl */
    open fun exposed(builder : DatabaseConfig.Builder.() -> Unit) {
        exposedDatabaseConfigFactory = { DatabaseConfig(builder) }
    }
}