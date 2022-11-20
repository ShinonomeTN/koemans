package com.shinonometn.koemans.web

/**
 * Vararg validator
 */
@ValidatorBuilderDsl
fun Validator.Configuration.vararg(hint: String = "invalid_value", logic: (String) -> Boolean) = validator(hint) { param ->
    when (param) {
        is String -> logic(param)
        is List<*> -> param.all { logic(it as String) }
        else -> false
    }
}