package com.shinonometn.koemans.exposed

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.slf4j.LoggerFactory
import kotlin.test.assertTrue

class FilterQuerySupportTest {
    private val logger = LoggerFactory.getLogger(FilterQuerySupportTest::class.java)

    private val database = Database.connect(HikariDataSource(HikariConfig().apply {
        jdbcUrl = "jdbc:sqlite:file:test_filter_query_support?mode=memory"
        maximumPoolSize = 1
    }))

    object TestTable : IntIdTable("test_table") {
        const val DDL = """
            create table if not exists test_table(
                id integer primary key,
                name varchar(255),
                age integer
            )
        """

        val name = varchar("name", 255)
        val age = integer("age")
    }

    @Before
    fun setup() {
        transaction(database) {
            exec(TestTable.DDL)
            TestTable.deleteAll()

            listOf(
                "alice" to 20, "bob" to 30, "charlie" to 40, "david" to 50,
                "edward" to 60, "frank" to 70, "george" to 80, "harry" to 90,
                "ian" to 90, "james" to 100, "kate" to 110, "lisa" to 120,
            ).forEach { (name, age) ->
                TestTable.insert {
                    it[TestTable.name] = name
                    it[TestTable.age] = age
                }
            }
        }
    }

    val optionMappingForMatching = FilterOptionMapping {
        "name" means { TestTable.name eq it.asString() }
        "age" means { TestTable.age eq it.asString().toInt() }
    }

    @Test
    fun `Test filter build`() {
        val filter = optionMappingForMatching(
            mapOf(
                "name" to listOf("test"),
                "age" to listOf("10")
            )
        )

        logger.info(filter.parameters.joinToString(","))
    }

    @Test
    fun `Test filter query none match`() {
        val filter = optionMappingForMatching(
            mapOf(
                "name" to listOf("test"),
                "age" to listOf("10")
            )
        )

        val result = transaction(database) {
            TestTable.selectBy(filter).map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(0, result.size)
    }

    @Test
    fun `Test filer query match result`() {
        val filter = optionMappingForMatching(
            mapOf(
                "name" to listOf("alice"),
                "age" to listOf("20")
            )
        )

        val result = transaction(database) {
            TestTable.selectBy(filter).map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(1, result.size)
        assertEquals("alice", result[0].first)
        assertEquals(20, result[0].second)
    }

    val optionMappingForRange = FilterOptionMapping {
        "age_start" means { TestTable.age greaterEq it.asString().toInt() }
        "age_end" means { TestTable.age lessEq it.asString().toInt() }
    }

    @Test
    fun `Test filter query range matched`() {
        val filter = optionMappingForRange(
            mapOf(
                "age_start" to listOf("40"),
                "age_end" to listOf("70")
            )
        )

        val result = transaction(database) {
            TestTable.selectBy(filter).map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(4, result.size)
    }

    @Test
    fun `Test with additional query match`() {
        val filter = optionMappingForRange(
            mapOf(
                "age_start" to listOf("20"),
                "age_end" to listOf("70")
            )
        )

        val result = transaction(database) {
            TestTable.selectBy(filter) { it and (TestTable.name eq "edward") }.map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(1, result.size)
        assertEquals("edward", result[0].first)
    }

    @Test
    fun `Test only additional query matched`() {
        val filter = optionMappingForRange(emptyMap())

        val result = transaction(database) {
            TestTable.selectBy(filter) { it and (TestTable.name eq "george") }.map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(1, result.size)
        assertEquals("george", result[0].first)
    }

    val mappingForOrOp = FilterOptionMapping {
        opBuilder = { OrOp(it.values.toList()) }
        "name" means { TestTable.name eq it.asString() }
        "age" means { TestTable.age eq it.asString().toInt() }
    }

    @Test
    fun `Test or op`() {
        val filtering = mappingForOrOp(mapOf(
            "name" to listOf("harry"),
            "age" to listOf("90")
        ))

        val result = transaction(database) {
            TestTable.selectBy(filtering).map {
                it[TestTable.name] to it[TestTable.age]
            }.toList()
        }

        assertEquals(2, result.size)
        assertTrue(result.map { it.first }.containsAll(listOf("harry", "ian")))
    }

}