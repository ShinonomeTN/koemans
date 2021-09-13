package com.shinonometn.koemans.web.spring

import com.typesafe.config.*
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.PropertySource
import org.springframework.core.io.Resource
import org.springframework.core.io.support.EncodedResource
import org.springframework.core.io.support.PropertySourceFactory
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.nio.file.Paths

private val PARSE_OPTION = ConfigParseOptions.defaults().apply {
    syntax = ConfigSyntax.CONF
}

private fun parseHoconFrom(resource: Resource): Config {
    return resource.inputStream.use { inputStream ->
        InputStreamReader(inputStream, StandardCharsets.UTF_8).use { reader ->
            ConfigFactory.parseReader(reader, PARSE_OPTION).resolve()
        }
    }
}

private fun toFlatMap(config: Config): Map<String, Any> {
    val properties = LinkedHashMap<String,Any>()
    toFlatMap(properties, "", config)
    return properties
}

private fun toFlatMap(properties : MutableMap<String,Any>, parentKey : String, config : Config) {
    val prefix = if ("" == parentKey) "" else "$parentKey."
    for ((key, value) in config.entrySet()) {
        val propertyKey = prefix + key
        addConfigValuePropertyTo(properties, propertyKey, value)
    }
}

private fun addConfigValuePropertyTo(properties: MutableMap<String, Any>, key: String, value: ConfigValue) {
    when (value) {
        is ConfigList -> processListValue(properties, key,  value)
        is ConfigObject -> processObjectValue(properties, key,  value)
        else -> processScalarValue(properties, key, value)
    }
}

private fun processListValue(properties: MutableMap<String, Any>, key: String, value: ConfigList) {
    if (value.isEmpty()) {
        addConfigValuePropertyTo(properties, key, ConfigValueFactory.fromAnyRef(""))
        return
    }
    for (i in value.indices) {
        // Used to properly populate lists in @ConfigurationProperties beans
        val propertyName = String.format("%s[%d]", key, i)
        val propertyValue = value[i]
        addConfigValuePropertyTo(properties, propertyName, propertyValue)
    }
}

private fun processObjectValue(properties: MutableMap<String, Any>, key: String, value: ConfigObject) {
    toFlatMap(properties, key,  value.toConfig())
}

private fun processScalarValue(properties: MutableMap<String, Any>, key: String, value: ConfigValue) {
    properties[key] = value.unwrapped()
}

class HoconPropertySource : PropertySourceFactory {
    override fun createPropertySource(name: String?, encoded: EncodedResource): PropertySource<*> {
        return buildPropertySourceFrom(name, encoded)
    }

    companion object {
        fun buildPropertySourceFrom(name : String, config : Config): PropertySource<*> {
            return MapPropertySource(name, toFlatMap(config))
        }

        fun buildPropertySourceFrom(name : String?, encoded: EncodedResource) : PropertySource<*> {
            val resource = encoded.resource
            val realName = name ?: resource.filename
            return MapPropertySource(realName, toFlatMap(parseHoconFrom(resource)))
        }
    }
}

fun SpringContextConfiguration.useHoconPropertySource(name : String, hoconConf : Config) {
    additionalActions.add {
        environment.propertySources.addFirst(HoconPropertySource.buildPropertySourceFrom(name, hoconConf))
    }
}

fun SpringContextConfiguration.useHoconPropertySource(name : String?, resource: Resource) {
    additionalActions.add {
        environment.propertySources.addFirst(HoconPropertySource.buildPropertySourceFrom(name, EncodedResource(resource, "UTF8")))
    }
}

