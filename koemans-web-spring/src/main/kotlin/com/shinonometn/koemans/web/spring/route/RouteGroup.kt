package com.shinonometn.koemans.web.spring.route

/**
 * Annotation for route groups.
 * See [Route.injectRouteGroup].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class RouteGroup(val name: String = "default")