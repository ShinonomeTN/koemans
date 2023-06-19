package com.shinonometn.koemans.web.spring.route

import io.ktor.routing.*

/**
 * Provide `route{}` dsl configuration features
 */
interface RouteProvider {
    fun Route.provide()

    companion object {
        operator fun invoke(body: Route.() -> Unit) = object : RouteProvider {
            override fun Route.provide() = body()
        }
    }
}