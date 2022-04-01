package com.shinonometn.koemans.web

import io.ktor.http.*
import org.junit.Assert.*
import org.junit.Test

class ValidatorTest {
    @Test
    fun `Test validate params`() {
        val validator = Validator {
            "username" with isString { it.length <= 255 }
            "password" with isString { it.length >= 8 }
        }

        val params = ParametersBuilder().apply {
            append("username", "username")
            append("password", "password")
        }.build()

        validator.validate(params)
    }

    @Test
    fun `Test validate params as list`() {
        val validator = Validator(Validator.Policy.Arrays) {
            "users" with isStringList { it.all { u -> u.isNotBlank() } }
        }

        val params = parseQueryString("users=1&users=2&users=3")

        validator.validate(params)
    }

    @Test(expected = ParamValidationException::class)
    fun `Test disallow unknown param`() {
        val validator = Validator {
            "users" with isString
            // default value is false
//            allowUnknownParams = false
        }

        val params = parametersOf("users" to listOf("1"), "unknown" to listOf("2"))

        validator.validate(params)
    }

    @Test
    fun `Test allow unknown param`() {
        val validator = Validator {
            "name" with isString
            allowUnknownParams = true
        }

        val params = parametersOf("name" to listOf("1"), "unknown" to listOf("2"))

        validator.validate(params)
    }
}