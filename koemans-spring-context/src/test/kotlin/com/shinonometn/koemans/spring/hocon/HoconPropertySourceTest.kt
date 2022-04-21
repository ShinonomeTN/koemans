package com.shinonometn.koemans.spring.hocon

import com.shinonometn.koemans.spring.HoconPropertySource
import com.typesafe.config.ConfigFactory
import org.junit.Assert
import org.junit.Test

class HoconPropertySourceTest {
    @Test
    fun `Test read`() {
        val properties = ConfigFactory.parseResources("hocon.conf").resolve()
        val source = HoconPropertySource.buildPropertySourceFrom("null", properties)
        Assert.assertEquals(source.getProperty("test.value1"), 86400)
        Assert.assertEquals(source.getProperty("test.value2"), 86400)
    }
}