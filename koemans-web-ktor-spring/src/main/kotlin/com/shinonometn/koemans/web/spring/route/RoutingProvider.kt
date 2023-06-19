package com.shinonometn.koemans.web.spring.route

import io.ktor.routing.*

/**
 * Provide `routing{}` dsl configuration features.
 */
interface RoutingProvider {
    fun Routing.provide()

    companion object {
        operator fun invoke(body : Routing.() -> Unit) = object : RoutingProvider {
            override fun Routing.provide() = body()
        }
    }
}