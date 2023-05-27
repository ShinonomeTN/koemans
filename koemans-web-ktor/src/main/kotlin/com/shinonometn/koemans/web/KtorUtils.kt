package com.shinonometn.koemans.web

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.util.pipeline.*
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

typealias KtorCallContext = PipelineContext<Unit, ApplicationCall>

/**
 * Get current HoconConfig from the Application's environment by reflection.
 *
 * returns null if current environment is not a HoconApplicationConfig
 */
fun Application.getEnvironmentHoconConfig() : Config? = environment.config.getOriginHoconConfig()

/**
 * Get origin hocon configuration from ApplicationConfig.
 * If it is not HoconApplicationConfig, return null.
 */
fun ApplicationConfig.getOriginHoconConfig() : Config? {
    if(this !is HoconApplicationConfig) return null
    val hoconType = Config::class.createType()
    val config = this
    val field = HoconApplicationConfig::class.declaredMemberProperties.firstOrNull {
        it.visibility == KVisibility.PRIVATE && it.returnType.isSubtypeOf(hoconType)
    }?.also { it.isAccessible = true } ?: return null

    return field.get(config) as Config
}