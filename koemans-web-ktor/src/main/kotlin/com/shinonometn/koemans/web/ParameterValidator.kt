package com.shinonometn.koemans.web

import io.ktor.http.*
import kotlin.collections.ArrayList
import kotlin.collections.Collection
import kotlin.collections.HashMap
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.all
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.zip

@DslMarker
annotation class ValidatorBuilderDsl

class ParamValidationException(message: String) : Exception(message) {
    val error: String = "param_validation_error"
}

class ValidationHandler(val logic: (Any?) -> Boolean, val message: String)

class Validator internal constructor(private val policy: Policy, private val config: Configuration) {

    class Configuration internal constructor() {
        val validatorMap = HashMap<String, MutableList<ValidationHandler>>()
        val nullTable = HashMap<String, Boolean>()
        var showValueWhenFailed = true
        var allowUnknownParams = false

        companion object {
            private val isStringValidator = ValidationHandler({ it is String }, "invalid_value")
            private val isStringListValidator = ValidationHandler({ it is List<*> && it.all { o -> o is String } }, "invalid_value")
        }

        @ValidatorBuilderDsl
        infix fun String.with(validator: ValidationHandler) {
            val list = validatorMap.computeIfAbsent(this) { ArrayList() }
            list.add(validator)
        }

        @ValidatorBuilderDsl
        infix fun String.with(validators: List<ValidationHandler>) {
            val list = validatorMap.computeIfAbsent(this) { ArrayList() }
            list.addAll(validators)
        }

        @ValidatorBuilderDsl
        infix fun Collection<String>.with(validator: ValidationHandler) {
            forEach { it.with(validator) }
        }

        @ValidatorBuilderDsl
        infix fun Collection<String>.with(validators: List<ValidationHandler>) {
            forEach { it.with(validators) }
        }

        @ValidatorBuilderDsl
        fun validator(hint: String = "invalid_value", logic: (Any?) -> Boolean): ValidationHandler {
            return ValidationHandler(logic, hint)
        }

        @ValidatorBuilderDsl
        val isString: ValidationHandler = isStringValidator

        @ValidatorBuilderDsl
        fun isString(hint: String = "invalid_value", logic: ((String) -> Boolean)? = null): ValidationHandler {
            return ValidationHandler({ it is String && logic?.invoke(it) ?: true }, hint)
        }

        val isStringList: ValidationHandler = isStringListValidator

        @ValidatorBuilderDsl
        @Suppress("UNCHECKED_CAST")
        fun isStringList(hint: String = "invalid_value", logic: ((List<String>) -> Boolean)? = null): ValidationHandler {
            return ValidationHandler({ it is List<*> && it.all { o -> o is String } && logic?.invoke(it as List<String>) ?: true }, hint)
        }

        @ValidatorBuilderDsl
        fun String.optional(optional: Boolean = true) {
            nullTable[this] = optional
        }

        @ValidatorBuilderDsl
        fun optional(field: String, optional: Boolean = true): String {
            nullTable[field] = optional
            return field
        }

        @ValidatorBuilderDsl
        fun optional(vararg fields: String, optional: Boolean = true): List<String> {
            for (field in fields) {
                nullTable[field] = optional
            }
            return fields.toList()
        }

        @ValidatorBuilderDsl
        fun exclude(vararg fields: String) {
            for (field in fields) {
                validatorMap.remove(field)
                nullTable.remove(field)
            }
        }
    }

    class Policy(val policy: List<ValidationHandler>.(key: String, params: Parameters, config: Configuration) -> Unit) {

        companion object {
            val FirstValue = Policy { key, params, config ->
                val value = params.getAll(key) ?: if (!config.nullTable.containsKey(key)) throw nonNullRequired(key)
                else return@Policy

                zip(value).forEach {
                    if (!it.first.logic(it.second)) throw config.invalidField(it.first.message, it.second)
                }
            }

            val Arrays = Policy { key, params, config ->
                val value = params.getAll(key) ?: if (!config.nullTable.containsKey(key)) throw nonNullRequired(key)
                else return@Policy

                forEach {
                    if (!it.logic(value)) throw config.invalidField(it.message, value)
                }
            }

            val Vararg = Policy { key, params, config ->
                val value = params.getAll(key) ?: if (!config.nullTable.containsKey(key)) throw nonNullRequired(key)
                else return@Policy

                forEach {
                    if (!it.logic(if (value.size != 1) value else value.first())) throw config.invalidField(it.message, value)
                }
            }

            private fun Configuration.invalidField(message: String, value: Any) = ParamValidationException(
                if (!showValueWhenFailed) message else "${message}:${value}"
            )

            private fun nonNullRequired(key: String) = ParamValidationException("param_non_null_required:$key")
        }
    }

    constructor(policy: Policy, configuration: Configuration.() -> Unit) : this(policy, Configuration()) {
        configuration.invoke(config)
    }

    constructor(configuration: Configuration.() -> Unit) : this(Policy.FirstValue, Configuration()) {
        configuration.invoke(config)
    }

    fun validate(parameters: Parameters, policy: Policy = this.policy): Parameters {
        val validators = config.validatorMap

        if(!config.allowUnknownParams) {
            parameters.forEach { k, _ ->
                if(!validators.keys.contains(k)) throw ParamValidationException("param_unknown_param:$k")
            }
        }

        validators.forEach { (key, validators) ->
            with(policy) {
                validators.policy(key, parameters, config)
            }
        }

        return parameters
    }

    fun copy(newPolicy: Policy = this.policy, block: (Configuration.() -> Unit)? = null): Validator {
        val newConfig = Configuration()
        config.validatorMap.forEach { (t, u) -> newConfig.validatorMap[t] = u }
        config.nullTable.forEach { (t, u) -> newConfig.nullTable[t] = u }

        block?.invoke(newConfig)

        return Validator(newPolicy, newConfig)
    }
}

@ValidatorBuilderDsl
fun Validator.Configuration.vararg(hint: String = "invalid_value", logic: (String) -> Boolean) = validator(hint) { param ->
    when (param) {
        is String -> logic(param)
        is List<*> -> param.all { logic(it as String) }
        else -> false
    }
}