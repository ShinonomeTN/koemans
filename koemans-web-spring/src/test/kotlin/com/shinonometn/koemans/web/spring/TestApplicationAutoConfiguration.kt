package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.ComponentScan

@ComponentScan("com.shinonometn.koemans.web")
open class TestApplicationAutoConfiguration(c: ApplicationContext) : RoutingProvider {

    @Value("\${application.title}")
    private lateinit var applicationTitle: String

    override fun Routing.provide() {
        get {
            call.respondText { "Hello world" }
        }

        get("/property") {
            call.respondText { "Application Title: ${applicationTitle}" }
        }
    }

}