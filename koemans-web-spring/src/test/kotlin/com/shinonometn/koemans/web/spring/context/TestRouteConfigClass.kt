package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.route.RouteProvider
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.springframework.stereotype.Component

@Component
class TestRouteConfigClass(private val config: TestApplicationAutoConfiguration) : RouteProvider {
    override fun Route.provide() {
        get { call.respondText { "Hello world" } }

        get("/property/title") {
            call.respondText { "Application Title: ${config.applicationTitle}" }
        }
    }
}