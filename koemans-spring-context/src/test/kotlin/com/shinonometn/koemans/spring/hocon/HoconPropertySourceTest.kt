package com.shinonometn.koemans.spring.hocon

import com.shinonometn.koemans.spring.HoconPropertySource
import com.typesafe.config.ConfigFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class HoconPropertySourceTest {
    @Test
    fun `Test read`() {
        val properties = ConfigFactory.parseResources("hocon.conf").resolve()
        val source = HoconPropertySource.buildPropertySourceFrom("hocon", properties)
        assertEquals(86400, source.getProperty("test.value1"))
        assertEquals(86400, source.getProperty("test.value2"))
    }
}