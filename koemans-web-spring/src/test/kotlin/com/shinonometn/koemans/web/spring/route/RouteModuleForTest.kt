package com.shinonometn.koemans.web.spring.route

import com.shinonometn.koemans.web.spring.configuration.configureBySpring
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller

fun Application.routeModuleForTest() {
    configureBySpring {
        annotationDriven(RouteTestApplicationConfiguration::class.java) {

        }
    }
}

@ComponentScan("com.shinonometn.koemans.web.spring.route")
open class RouteTestApplicationConfiguration {
    @Bean
    open fun rootRouting() = RoutingProvider {
        injectRouteGroup("default_routing_group")
        injectRoute<TestRouteConfigClass>()
    }
}

@Controller
@KtorRoute("/ktor")
class TestController {

    @KtorRoute
    fun Route.hello2() = get {
        call.respond("HelloWorld2!")
    }

    @KtorRoute("/route")
    fun Route.hello() = get {
        call.respondText { "HelloWorld!" }
    }
}

@Component
class TestRouteConfigClass : RouteProvider {
    override fun Route.provide() {
        get { call.respondText { "Hello world" } }
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