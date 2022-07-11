package com.shinonometn.koemans.web

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.util.pipeline.*
import kotlin.reflect.KVisibility
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

typealias KtorCallContext = PipelineContext<Unit, ApplicationCall>

/**
 * Get current HoconConfig from the Application's environment by reflection.
 *
 * returns null if current environment is not a HoconApplicationConfig
 */
fun Application.getEnvironmentHoconConfig() : Config? {
    val envConfig = environment.config
    return if(envConfig is HoconApplicationConfig) {
        val classMembers = HoconApplicationConfig::class.members
        val configMember = classMembers.first {
            it.visibility == KVisibility.PRIVATE && it.returnType == Config::class.starProjectedType
        }
        configMember.isAccessible = true
        configMember.call(envConfig) as Config
    } else null
}
