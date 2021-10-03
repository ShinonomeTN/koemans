package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.core.io.ClassPathResource

fun Application.mainTestModule() {
    install(SpringContext) {
        annotationDriven(TestApplicationAutoConfiguration::class.java) {
            useHoconPropertySource(null, ClassPathResource("application.hocon"))
        }
    }

    routing {
        installSpringRoutingConfigurations()
    }
}

class SpringContextTest {

    @Test
    fun `Test context load`() {
        withTestApplication(Application::mainTestModule) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello world", response.content)
            }
        }
    }

    @Test
    fun `Test context property load`() {
        withTestApplication(Application::mainTestModule) {
            handleRequest(HttpMethod.Get, "/property").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Application Title: Hello world app", response.content)
            }
        }
    }
}