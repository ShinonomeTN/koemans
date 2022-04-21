package com.shinonometn.koemans.spring.context

import com.shinonometn.koemans.spring.annotationDrivenApplicationContext
import com.shinonometn.koemans.spring.find
import com.shinonometn.koemans.spring.propertySourcePlaceholderSupport
import com.shinonometn.koemans.spring.useHoconPropertySource
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
        context.start()

        val mainConfig = context.find<TestApplicationAutoConfiguration>()
        assertEquals("Hello World!", mainConfig.applicationTitle)

        val bean1 = context.find<TestPropertySourcePlaceHolder>()
        assertEquals(1, bean1.number1)
        assertTrue(bean1.boolean2)
    }
}