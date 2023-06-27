package com.shinonometn.koemans.exposed

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortQuerySupportTest {
    private val database = Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:file:test_sort_query_support?mode=memory"
        maximumPoolSize = 1
    }))

    object TestTable : IntIdTable("test_table_2") {
        const val DDL = """
            create table if not exists test_table_2(
                id integer primary key,
                name varchar(255),
                age integer
            )
        """

        val colName = varchar("name", 255)
        val colAge = integer("age")

        fun newRecord(name : String, age : Int) = insert {
            it[colName] = name
            it[colAge] = age
        }
    }

    @Before
    fun setup() {
        transaction(database) {
            exec(TestTable.DDL)
            TestTable.deleteAll()

            listOf(
                "alice" to 120,
                "bob" to 110,
                "charlie" to 100,
                "david" to 90,
                "edward" to 90,
                "frank" to 80,
                "george" to 70,
                "harry" to 60,
                "ian" to 50,
                "james" to 40,
                "kate" to 30,
                "lisa" to 20,
            ).forEach { (name, age) -> TestTable.newRecord(name, age) }
        }
    }

    private val sortOptionMapping = SortOptionMapping {
        "name" associateTo TestTable.colName defaultOrder DESC
        "age" associateTo TestTable.colAge defaultOrder ASC
    }

    @Test
    fun `Test default sorting`() {
        val sorting = sortOptionMapping(emptyList())
        val alphabets = transaction(database) {
            TestTable.selectAll().orderBy(sorting).map { it[TestTable.colName][0] }.joinToString("")
        }
        assertEquals("abcdefghijkl", alphabets)
    }

    @Test
    fun `Test sort with params`() {
        val sorting = sortOptionMapping(listOf(
            "name" to "desc"
        ))
        val alphabets = transaction(database) {
            TestTable.selectAll().orderBy(sorting).map { it[TestTable.colName][0] }.joinToString("")
        }
        assertEquals("abcdefghijkl".reversed(), alphabets)
    }

    @Test
    fun `Test sort with implied params`() {
        val sortOptionMappingWithImplied = SortOptionMapping {
            ("name" associateTo TestTable.colName defaultOrder DESC).implied()
        }

        val sorting = sortOptionMappingWithImplied(emptyList())
        val alphabets = transaction(database) {
            TestTable.selectAll().orderBy(sorting).map { it[TestTable.colName][0] }.joinToString("")
        }
        assertEquals("abcdefghijkl".reversed(), alphabets)
    }

    @Test
    fun `Test sort with implied order`() {
        val sortOptionMappingImpliedAltered = sortOptionMapping.copy {
            // this method does not change the origin field declaring orders
            impliedFields("age", "name")
        }

        val sorting = sortOptionMappingImpliedAltered()
        val pairs = transaction(database) {
            TestTable.selectAll().orderBy(sorting).map { it[TestTable.colName][0] to it[TestTable.colAge] }.toList()
        }
        val alphabets = pairs.map { it.first }.joinToString("")
        assertEquals("abcdefghijkl".reversed(), alphabets)

        val slice = pairs.drop(2).take(2).map { it.second }
        assertTrue(slice[0] < slice[1], "First should lesser than last")
    }

    @Test
    fun `Test sort with order`() {
        val sorting = sortOptionMapping(listOf(
            "age" to "desc",
            "name" to "ASC"
        ))

        val pairs = transaction(database) {
            TestTable.selectAll().orderBy(sorting).map { it[TestTable.colName][0] to it[TestTable.colAge] }.toList()
        }

        assertTrue(pairs.first().second > pairs.last().second, "The first age should larger than the last age.")
        assertEquals("de", pairs.filter { it.second == 90 }.map { it.first }.joinToString(""))
    }
}