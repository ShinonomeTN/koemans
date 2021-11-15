package com.shinonometn.koemans.exposed.database.sqlite

import com.shinonometn.koemans.exposed.database.sqlDatabase
import org.junit.Assert.*
import org.junit.Test

class Sqlite3Test {

    @Test
    fun `Test open database`() {
        val sqlite3 = sqlDatabase(Sqlite3) {
            inMemory("test_database")
            poolingStrategy = HikariPooling
        }

        sqlite3 {
            exec("create table tb_test(id integer primary key, text text)")
            exec("insert into tb_test(text) values('Hello world')")
        }

        sqlite3 {
            exec("select text from tb_test") {
                assertTrue("It should have at least 1 record.",it.next())
                assertTrue("Record content should be 'Hello World'.",it.getString(1) == "Hello world")
            }
        }
    }
}