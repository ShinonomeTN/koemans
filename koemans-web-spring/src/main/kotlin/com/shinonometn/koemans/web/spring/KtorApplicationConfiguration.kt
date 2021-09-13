package com.shinonometn.koemans.web.spring

import io.ktor.application.*

/*
*
* Providing ktor application configuration feature
*
*/
interface KtorApplicationConfiguration {
    val priority: Double
        get() = 1.0

    fun Application.configure()
}

fun Application.installSpringFeatureConfiguration() {
    val featureConfigs = springContext.getBeansOfType(KtorApplicationConfiguration::class.java).values

    featureConfigs.sortedByDescending { it.priority }.forEach {
        with(it) { configure() }
    }

    log.info("Installed {} feature configurations.", featureConfigs.count())
}