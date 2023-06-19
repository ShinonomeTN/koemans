package com.shinonometn.koemans.web.spring.configuration

import com.shinonometn.koemans.web.spring.SpringContext
import com.shinonometn.koemans.web.spring.route.routingConfigurationBySpring
import com.shinonometn.koemans.web.spring.springContext
import io.ktor.application.*
import org.springframework.beans.factory.getBeansWithAnnotation
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KCallable
import kotlin.reflect.full.createType
import kotlin.reflect.full.extensionReceiverParameter
import kotlin.reflect.full.hasAnnotation

/**
 * Providing ktor application auto configuration feature
 */
@Deprecated("Use @KtorConfiguration on @Configuration beans instead.")
interface KtorApplicationConfiguration {
    val priority: Double
        get() = 1.0

    fun Application.configure()
}

/**
 * Apply all configuration actions that provided by [KtorApplicationConfiguration] beans.
 */
@Deprecated("Use configureBySpring() instead.")
fun Application.installSpringFeatureConfiguration() {
    val featureConfigs = springContext.getBeansOfType(KtorApplicationConfiguration::class.java).values

    featureConfigs.sortedByDescending { it.priority }.forEach {
        with(it) { configure() }
    }

    log.info("Installed {} feature configurations.", featureConfigs.count())
}

class KtorConfigurationInfo(val priority: Double, val configuration: Any, val function: KCallable<*>)

private fun Application.prepareKtorConfigurations(springContext: ApplicationContext) {
    val configurations = springContext.getBeansWithAnnotation<Configuration>().values
    val applicationType = Application::class.createType()
    val infos = configurations.flatMap { configuration ->
        configuration::class.members.filter {
            it.hasAnnotation<KtorConfiguration>() && (applicationType == it.extensionReceiverParameter?.type)
        }.map {
            val annotation = it.annotations.filterIsInstance<KtorConfiguration>().first()
            KtorConfigurationInfo(annotation.priority, configuration, it)
        }.sortedByDescending { it.priority }
    }
    SpringContext.logger.info("Found {} @KtorConfiguration.", infos.size)
    infos.forEach {
        it.function.call(it.configuration, this)
    }
}

/**
 * Configure Ktor Application using SpringContext.
 * It will execute all methods in @Configuration beans with annotation @KtorConfiguration
 * And install routes from all RoutingProvider, RouteProvider and methods with @KtorRoute in @Controller
 * beans
 *
 * @param configure SpringContext additional configuration
 */
fun Application.configureBySpring(configure: SpringContext.Configuration.() -> Unit) {
    install(SpringContext, configure)

    prepareKtorConfigurations(springContext)

    routingConfigurationBySpring()
}