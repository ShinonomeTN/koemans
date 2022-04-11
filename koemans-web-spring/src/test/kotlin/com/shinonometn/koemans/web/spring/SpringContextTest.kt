package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.testing.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

@ComponentScan("com.shinonometn.koemans.web.spring")
open class TestApplicationAutoConfiguration {

    @Value("\${application.title}")
    lateinit var applicationTitle: String
        private set

    @Bean
    open fun rootRouting() = RoutingProvider {
        injectRouteGroup("default_routing_group")
        injectRoute<TestRouteConfigClass>()
    }
}

@Configuration
open class TestRouteInjectionConfiguration {

    @Bean
    @RouteGroup("default_routing_group")
    open fun indexRoute() = RouteProvider {
        get("/group") {
            call.respondText { "Installed from default route group." }
        }
    }

}

@Component
class TestRouteConfigClass(private val config: TestApplicationAutoConfiguration) : RouteProvider {
    override fun Route.provide() {
        get { call.respondText { "Hello world" } }

        get("/property/title") {
            call.respondText { "Application Title: ${config.applicationTitle}" }
        }
    }
}

fun Application.mainTestModule() {
    install(SpringContext) {
        annotationDriven(TestApplicationAutoConfiguration::class.java) {
            propertySourcePlaceholderSupport()
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
}