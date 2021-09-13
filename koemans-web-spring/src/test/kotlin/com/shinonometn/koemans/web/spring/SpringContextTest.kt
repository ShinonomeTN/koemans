package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.testing.*
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Test
import org.springframework.core.io.ClassPathResource

fun Application.mainModule() {
    install(SpringContext) {
        annotationDriven(TestApplicationAutoConfiguration::class.java) {
            useHoconPropertySource(null, ClassPathResource("application.hocon"))
        }
    }

    routing {
        installSpringRoutingConfigurations()
    }
}

@Ignore
class SpringContextTest {

    @Test
    fun `Test context load`() {
       withTestApplication(Application::mainModule) {
           handleRequest(HttpMethod.Get, "/").apply {
               assertEquals(HttpStatusCode.OK, response.status())
               assertEquals("Hello world", response.content)
           }
       }
    }

    @Test
    fun `Test context property load`() {
        withTestApplication(Application::mainModule) {
            handleRequest(HttpMethod.Get, "/property").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Application Title: Hello world app", response.content)
            }
        }
    }
}