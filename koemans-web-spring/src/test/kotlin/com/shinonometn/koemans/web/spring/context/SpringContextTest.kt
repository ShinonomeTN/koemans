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
    fun `Test basic request`() {
        withTestApplication(Application::mainTestModule) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello world", response.content)
            }
        }
    }

    @Test
    fun `Test route group install`() {
        withTestApplication(Application::mainTestModule) {
            handleRequest(HttpMethod.Get, "/group").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Installed from default route group.", response.content)
            }
        }
    }

    @Test
    fun `Test context property load`() {
        withTestApplication(Application::mainTestModule) {
            handleRequest(HttpMethod.Get, "/property/title").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Application Title: Hello world app", response.content)
            }
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