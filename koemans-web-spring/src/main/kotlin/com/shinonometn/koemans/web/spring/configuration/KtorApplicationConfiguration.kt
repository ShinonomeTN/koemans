package com.shinonometn.koemans.web.spring.configuration

import com.shinonometn.koemans.web.spring.springContext
import io.ktor.application.*

/**
 * Providing ktor application auto configuration feature
 */
interface KtorApplicationConfiguration {
    val priority: Double
        get() = 1.0

    fun Application.configure()
}

/**
 * Apply all configuration actions that provided by [KtorApplicationConfiguration] beans.
 */
fun Application.installSpringFeatureConfiguration() {
    val featureConfigs = springContext.getBeansOfType(KtorApplicationConfiguration::class.java).values

    featureConfigs.sortedByDescending { it.priority }.forEach {
        with(it) { configure() }
    }

    log.info("Installed {} feature configurations.", featureConfigs.count())
}