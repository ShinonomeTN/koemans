package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.routing.*

/*
*
* Provide Routing configuration features
*
*/

interface RoutingProvider {
    fun Routing.provide()
}

@RouteGroup
interface RouteProvider {
    fun Route.provide()
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class RouteGroup(val name: String = "default")

inline fun <reified T : RouteProvider> Route.injectRoute() {
    val provider = application.springContext.find<T>()
    with(provider) { this@injectRoute.provide() }
    application.log.info("Installed route from '{}' on '{}'.", provider::class.simpleName, this.toString())
}

inline fun <reified T : Annotation> Route.injectRouteGroup(selector: (T) -> Boolean = { true }) {
    val providers = application.springContext.getBeansWithAnnotation(T::class.java).values.filterIsInstance<RouteProvider>().filter {
        selector(it::class.annotations.filterIsInstance<T>().first())
    }.takeUnless {
        it.isEmpty()
    } ?: return application.log.info("No route installed on '{}' because no provider in group '@{}'.", T::class.simpleName)

    providers.forEach { with(it) { this@injectRouteGroup.provide() } }

    application.log.info("Installed {} route provider(s) from group '@{}' on '{}'.", providers.size, T::class.simpleName, this.toString())
}

fun Route.injectRouteGroup(name: String) {
    val providers = application.springContext.getBeansWithAnnotation(RouteGroup::class.java).values.filterIsInstance<RouteProvider>().filter {
        val annotations = it::class.annotations.filterIsInstance<RouteGroup>()
        if (annotations.isEmpty()) false else annotations.first().name == name
    }.takeUnless {
        it.isEmpty()
    } ?: return application.log.info("No route installed on '{}' because no provider in group '{}'.", this.toString(), name)

    providers.forEach { with(it) { this@injectRouteGroup.provide() } }

    application.log.info("Installed {} route provider(s) from group '{}' on '{}'.", providers.size, name, this.toString())
}

fun Routing.installSpringRoutingConfigurations() {
    val configs = application.springContext.getBeansOfType(RoutingProvider::class.java).values.takeUnless {
        it.isEmpty()
    } ?: return application.log.warn("No routing configurations.")

    configs.forEach { with(it) { this@installSpringRoutingConfigurations.provide() } }
    application.log.info("Installed {} routing configurations.", configs.size)
}
