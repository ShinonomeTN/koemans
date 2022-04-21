package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.route.RouteGroup
import com.shinonometn.koemans.web.spring.route.RouteProvider
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

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