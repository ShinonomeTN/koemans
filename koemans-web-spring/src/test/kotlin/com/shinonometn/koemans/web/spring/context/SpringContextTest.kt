package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertTrue

class SpringContextTest {

    @Test
    fun `Test context load`() {
        withTestApplication(Application::mainTestModule) {
            application.springContext
        }
    }

    @Test
    fun `Test property source place holder`() {
        withTestApplication(Application::mainTestModule) {
            val test = application.springContext.find<TestPropertySourcePlaceHolder>()
            assertTrue(test.number == 1, "number should equals to 1")
        }
    }
}