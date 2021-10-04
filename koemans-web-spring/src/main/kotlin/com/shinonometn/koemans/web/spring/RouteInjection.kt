package com.shinonometn.koemans.web.spring

import io.ktor.application.*
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

/**
 * Annotation for route groups.
 * See [Route.injectRouteGroup].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RouteGroup(val name: String = "default")

/**
 * Apply route configurations that provided by type [T]
 */
inline fun <reified T : RouteProvider> Route.injectRoute() {
    val provider = application.springContext.find<T>()
    with(provider) { this@injectRoute.provide() }
    application.log.info("Installed route from '{}' on '{}'.", provider::class.simpleName, this.toString())
}

/**
 * Apply route configurations by specified annotations, with an optional [selector] for
 * annotation filtering.
 */
inline fun <reified T : Annotation> Route.injectRouteGroup(selector: (T) -> Boolean = { true }) {
    val providers = application.springContext.getBeansWithAnnotation(T::class.java).values.filterIsInstance<RouteProvider>().filter {
        selector(it::class.annotations.filterIsInstance<T>().first())
    }.takeUnless {
        it.isEmpty()
    } ?: return application.log.info("No route installed on '{}' because no provider in group '@{}'.", T::class.simpleName)

    providers.forEach { with(it) { this@injectRouteGroup.provide() } }

    application.log.info("Installed {} route provider(s) from group '@{}' on '{}'.", providers.size, T::class.simpleName, this.toString())
}

/**
 * Apply route configurations by route groups that matches a given [name].
 */
fun Route.injectRouteGroup(name: String) {
    val context = application.springContext
    val log = application.log

    // Find providers
    val providers = context.getBeansWithAnnotation(RouteGroup::class.java).mapNotNull { e ->
        when {
            // Accept RouteProvider only
            e.value !is RouteProvider -> null

            // Given name matches the annotation on bean (when exists)
            name != context.findAnnotationOnBean(e.key, RouteGroup::class.java)?.name -> null

            // Return entry value
            else -> e.value as RouteProvider
        }
    }.takeUnless {
        it.isEmpty()
    } ?: return log.info("No route installed on '{}' because no provider in group '{}'.", this.toString(), name)

    providers.forEach { with(it) { this@injectRouteGroup.provide() } }

    log.info("Installed {} route provider(s) from group '{}' on '{}'.", providers.size, name, this.toString())
}

/**
 * Automatically install routing configuration (usually a root routing configuration) by
 * [RoutingProvider]s and [RouteProvider]s in Spring context.
 *
 * If has any bean name conflict, Spring will not load the later one (according to dependencies)
 * with the same name.
 */
fun Routing.installSpringRoutingConfigurations() {
    val context = application.springContext

    val configs = context.getBeansOfType(RoutingProvider::class.java).values.takeUnless {
        it.isEmpty()
    } ?: return application.log.warn("No routing configurations.")

    configs.forEach { with(it) { this@installSpringRoutingConfigurations.provide() } }
    application.log.info("Installed {} routing configurations.", configs.size)
}
