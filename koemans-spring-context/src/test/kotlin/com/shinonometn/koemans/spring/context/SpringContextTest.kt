package com.shinonometn.koemans.spring.context

import com.shinonometn.koemans.spring.*
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SpringContextTest {
    @Test
    fun `Test Load Context`() {
        annotationDrivenApplicationContext().apply {
            refresh()
            start()
        }
    }

    @Test
    fun `Test config bean`() {
        val context = annotationDrivenApplicationContext(TestApplicationAutoConfiguration::class.java) {
            propertySourcePlaceholderSupport()
            useHoconPropertySource("application", ClassPathResource("application.conf"))
        }
        context.bootstrap()

        val mainConfig = context.find<TestApplicationAutoConfiguration>()
        assertEquals("Hello World!", mainConfig.applicationTitle)

        val bean1 = context.find<TestPropertySourcePlaceHolder>()
        assertEquals(1, bean1.number1)
        assertTrue(bean1.boolean2)
    }
}