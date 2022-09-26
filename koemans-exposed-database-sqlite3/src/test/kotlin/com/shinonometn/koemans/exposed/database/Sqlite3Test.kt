package com.shinonometn.koemans.exposed.database

import com.shinonometn.koemans.exposed.datasource.HikariDatasource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import kotlin.random.Random

@Suppress("SqlResolve")
class Sqlite3Test {

    @Test
    fun `Test open database`() {
        val sqlite3 = sqlDatabase(Sqlite3) {
            inMemory("test_database")
            dataSource = HikariDatasource()

            exposed {

            }
        }

        sqlite3 {
            exec("create table tb_test(id integer primary key, text text)")
            exec("insert into tb_test(text) values('Hello world')")
        }

        sqlite3 {
            exec("select text from tb_test") {
                assertTrue("It should have at least 1 record.", it.next())
                assertTrue("Record content should be 'Hello World'.", it.getString(1) == "Hello world")
            }
        }
    }

    @Test
    fun `Test open database 2`() {
        sqlDatabase(Sqlite3) {
            inMemory("test_database")
            dataSource = HikariDatasource {
                connectionTimeout = 1000
                idleTimeout = 1000
                maxLifetime = 1000
                maximumPoolSize = 1
            }
        }
    }

    @Test
    fun `Test read and write`() {
        val file = Files.createTempDirectory("test_database").toFile().apply {
            deleteOnExit()
        }

        val readOnly = sqlDatabase(Sqlite3) {
            inFile("test_database", file.toPath())
            dataSource = HikariDatasource {
                maximumPoolSize = 20
            }
        }

        val writeOnly = sqlDatabase(Sqlite3) {
            inFile("test_database", file.toPath())
            dataSource = HikariDatasource()
        }

        writeOnly {
            exec("create table tb_test(id integer primary key, text text)")
        }

        runBlocking {
            (1..100).map {
                async(Dispatchers.IO) {
                    writeOnly {
                        exec("insert into tb_test(text) values('Hello world')")
                    }
                }
            }.awaitAll()
        }

        runBlocking {
            (1..1000).map {
                async(Dispatchers.IO) {
                    readOnly {
                        exec("select text from tb_test limit 1") {
                            assertTrue("It should have at least 1 record.", it.next())
                            assertTrue("Record content should be 'Hello World'.", it.getString(1) == "Hello world")
                        }
                    }
                }
            }.awaitAll()
        }

        runBlocking {
            readOnly {
                exec("select count(id) from tb_test") {
                    assertTrue("It should have at least 1 record.", it.next())
                    assertTrue("Record content should be '100'.", it.getInt(1) == 100)
                }
            }
        }
    }

    @Test(expected = ExposedSQLException::class)
    fun `Test read and write in memory`() {
        val database = sqlDatabase(Sqlite3) {
            inMemory("in_memory_rw_test")
            dataSource = HikariDatasource {
                maximumPoolSize = 20
            }
        }

        database { exec("create table tb_test(id integer primary key, text text)") }

        val actions = listOf(
            { "insert into tb_test(text) values ('${System.currentTimeMillis()}')" },
            { "delete from tb_test where id < ${Random.nextInt()}" }
        )

        runBlocking { (1..1000).map { async(Dispatchers.IO) { database { exec(actions[0]()) } } }.awaitAll() }

        val count = database { exec("select count(id) from tb_test") { it.next(); it.getInt(1) } }
        assertEquals("Should have 1000 database rows", 1000, count)

        runBlocking { (1..10000).map { async(Dispatchers.IO) { database { exec(actions[Random.nextInt(2)]()) } } } }
    }
}