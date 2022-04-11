package com.shinonometn.koemans.web.spring

import com.typesafe.config.ConfigFactory
import org.junit.Assert.*
import org.junit.Test

class HoconPropertySourceTest {
    @Test
    fun `Test read`() {
        val properties = ConfigFactory.parseResources("test.hocon").resolve()
        val source = HoconPropertySource.buildPropertySourceFrom("null", properties)
        assertEquals(source.getProperty("test.value"), 86400)
        assertEquals(source.getProperty("test.value2"), 86400)
    }
}