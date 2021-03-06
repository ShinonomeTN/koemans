package com.shinonometn.koemans.exposed.database

import com.shinonometn.koemans.utils.UrlQueryParameter
import org.jetbrains.exposed.sql.Database
import org.mariadb.jdbc.Driver
import javax.sql.DataSource

class MariaDB(val name: String, override val db: Database, override val datasource: DataSource?) : SqlDatabase {

    class Configuration : SqlDatabaseConfiguration() {
        override var name: String = ""
        override var driverClassName = Driver::class.qualifiedName!!
        override var urlFactory: () -> String = this::buildUrl

        override val supportUsernamePassword = true

        public override var username : String? = ""
        public override var password: String? = ""

        private val urlParams = UrlQueryParameter()
        fun parameters(builder: UrlQueryParameter.() -> Unit) {
            urlParams.builder()
        }

        private var hostInfo: String = "127.0.0.1:3306"

        var database: String? = null
            set(value) {
                field = value
                this.name = value ?: ""
            }

        fun localhost() {
            hostInfo = "jdbc:mariadb://127.0.0.1:3306/"
        }


        /** Configure database host info with [address] and [port] */
        fun host(address: String = "127.0.0.1", port: Int = 3306) {
            hostInfo = "$address:$port"
        }

        private fun buildUrl(): String {
            val database = this.database ?: error("Please provide database name.")
            val url = "jdbc:mariadb://$hostInfo/$database"
            return if (urlParams.isNotEmpty()) "$url?${urlParams.toUrlEncoded()}" else url
        }
    }

    companion object : SqlDatabaseType<MariaDB, Configuration>({ Configuration() }) {
        override fun createNewDatabase(config: Configuration, database: Database, dataSource: DataSource?): MariaDB {
            return MariaDB(config.name, database, dataSource)
        }
    }
}