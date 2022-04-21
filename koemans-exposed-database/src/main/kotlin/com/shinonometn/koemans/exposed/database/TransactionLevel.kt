package com.shinonometn.koemans.exposed.database

import java.sql.Connection

enum class TransactionLevel(val level: Int) {
    NONE(Connection.TRANSACTION_NONE),
    ALLOW_UNCOMMITTED(Connection.TRANSACTION_READ_COMMITTED),
    REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
    STRICT(Connection.TRANSACTION_SERIALIZABLE)
}