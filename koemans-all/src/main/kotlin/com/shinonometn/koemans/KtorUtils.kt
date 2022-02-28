package com.shinonometn.koemans

import com.typesafe.config.Config
import io.ktor.application.*
import io.ktor.config.*
import kotlin.reflect.KVisibility
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.isAccessible

/**
 * Get current HoconConfig from the Application's environment by reflection.
 *
 * returns false if current environment is not an HoconApplicationConfig
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
