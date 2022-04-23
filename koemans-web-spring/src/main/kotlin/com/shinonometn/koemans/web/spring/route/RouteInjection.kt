package com.shinonometn.koemans.web.spring.route

import com.shinonometn.koemans.spring.find
import com.shinonometn.koemans.web.spring.SpringContext
import com.shinonometn.koemans.web.spring.springContext
import io.ktor.application.*
import io.ktor.routing.*
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Controller
import kotlin.reflect.KFunction
import kotlin.reflect.full.*

/**
 * Apply route configurations that provided by type [T]
 */
inline fun <reified T : RouteProvider> Route.injectRoute() {
    val provider = application.springContext.find<T>()
    with(provider) { this@injectRoute.provide() }
    SpringContext.logger.info("Installed route from '{}' on '{}'.", provider::class.simpleName, this.toString())
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
    } ?: return SpringContext.logger.info("No route installed on '{}' because no provider in group '@{}'.", T::class.simpleName)

    providers.forEach { with(it) { this@injectRouteGroup.provide() } }

    SpringContext.logger.info("Installed {} route provider(s) from group '@{}' on '{}'.", providers.size, T::class.simpleName, this.toString())
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
    val logger = SpringContext.logger

    val configs = context.getBeansOfType(RoutingProvider::class.java).values.takeUnless {
        it.isEmpty()
    } ?: return logger.warn("No RoutingProvider configurations.")

    configs.forEach { with(it) { this@installSpringRoutingConfigurations.provide() } }
    logger.info("Installed {} routing configurations.", configs.size)
}

class KtorRouteInfo(val rootPath : String?, val path : String?, val controller : Any, val function : KFunction<*>)

private fun prepareRoutingConfiguration(springContext : ApplicationContext) : Routing.() -> Unit {
    val logger = SpringContext.logger

    val routingProviders: Collection<RoutingProvider> = springContext.getBeansOfType(RoutingProvider::class.java).values
    val routeClazzType = Route::class.createType()
    val controllerMethods = springContext.getBeansWithAnnotation<Controller>().values.flatMap { controller ->
        // Get @KtorRoute on Controller
        val rootPath = controller::class.annotations.filterIsInstance<KtorRoute>().firstOrNull()?.route
        controller::class.functions.filter {
            it.hasAnnotation<KtorRoute>() && (it.parameters.size == 2) && (routeClazzType == it.extensionReceiverParameter?.type)
        }.map { kFunction ->
            // Get @KtorRoute on Controller method
            val annotation = kFunction.annotations.filterIsInstance<KtorRoute>().first()
            KtorRouteInfo(rootPath,annotation.route, controller, kFunction)
        }
    }

    return Routing@{
        logger.info("Installing {} RoutingProvider.", routingProviders.size)
        routingProviders.forEach { with(it) { this@Routing.provide() } }
        logger.info("Installing {} @KtorRoute.", controllerMethods.size)
        controllerMethods.forEach { ktorRouteInfo ->
            // if root path is not empty, change root path
            var root = ktorRouteInfo.rootPath?.let { createRouteFromPath(it) } ?: this

            // if method path is not empty, change path
            root = ktorRouteInfo.path?.let { root.createRouteFromPath(it) } ?: this

            // Apply the path
            ktorRouteInfo.function.call(ktorRouteInfo.controller, root)
        }
    }
}

fun Application.routingConfigurationBySpring() = routing(prepareRoutingConfiguration(springContext))