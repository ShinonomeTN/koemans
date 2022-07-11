package com.shinonometn.koemans.web.feature

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.Test
import kotlin.test.assertEquals

private fun Application.moduleGlobalInboundInterceptorTest() {
    install(GlobalInboundInterceptor) {
        addInterceptor {
            if(it.call.request.headers["X-Test"] == "intercept") {
                it.call.respond(HttpStatusCode.OK, "Intercepted")
                terminate()
            }
        }
    }

    routing {
        get("/") {
            call.respond(HttpStatusCode.OK, "Hello")
        }

        get("/intercept") {
            call.respond(HttpStatusCode.OK, "It will never be reached.")
        }
    }
}

class GlobalInboundInterceptorTest {
    @Test
    fun `Test intercept and terminate`() {
        withTestApplication(Application::moduleGlobalInboundInterceptorTest) {
            handleRequest(HttpMethod.Get, "/intercept") {
                addHeader("X-Test", "intercept")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Intercepted", response.content)
            }
        }
    }

    @Test
    fun `Test not intercepted`() {
        withTestApplication(Application::moduleGlobalInboundInterceptorTest) {
            handleRequest(HttpMethod.Get, "/").apply {
                assertEquals("Hello", response.content)
            }
        }
    }
}