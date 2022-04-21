package com.shinonometn.koemans.web.spring.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Assert
import org.junit.Test
import kotlin.test.assertEquals

class RouteTest {

    @Test
    fun `Test @KtorRoute install`() {
        withTestApplication(Application::routeModuleForTest) {
            handleRequest(HttpMethod.Get, "/ktor/route").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("HelloWorld!", response.content)
            }
        }
    }

    @Test
    fun `Test route group install`() {
        withTestApplication(Application::routeModuleForTest) {
            handleRequest(HttpMethod.Get, "/group").apply {
                Assert.assertEquals(HttpStatusCode.OK, response.status())
                Assert.assertEquals("Installed from default route group.", response.content)
            }
        }
    }
}