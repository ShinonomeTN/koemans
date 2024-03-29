package com.shinonometn.koemans.utils

/**
 * Type alias for UrlParameters.
 * Url parameters are just simple string to string-list map
 */
typealias UrlParameters = Map<String, List<String>>

/** Create url parameters from [pair]s of key-list */
fun urlParametersOf(vararg pair: Pair<String, List<String>>) = pair.toMap()

/** Create url parameters from [pair]s */
@JvmName("urlParametersOfStringPairs")
fun urlParametersOf(vararg pair: Pair<String, String>) = pair.associate { it.first to listOf(it.second) }

private val EMPTY_IMMUTABLE: UrlParameters = emptyMap()

/** create a empty url parameter map */
fun emptyUrlParametersOf(): UrlParameters = EMPTY_IMMUTABLE

/** create a empty url parameter map */
fun urlParametersOf() = emptyUrlParametersOf()

fun Collection<Pair<String, String>>.toUrlParameters(): Map<String, List<String>> =
    groupBy { it.first }.mapValues { entry -> entry.value.map { it.second } }

/**
 * Convert to url-encoded parameter string
 *
 * @param dense use dense list format (like: `key1=value1,value2&key2=value1`), default is true
 *              if not sure, set it false to use flatten list format for better compatibility (like: `key1=value1&key1=value2`)
 */
@JvmName("urlEncodedStringStringMap")
fun UrlParameters.urlEncoded(dense: Boolean = true) = when {
    isEmpty() -> ""
    dense -> entries.joinToString("&") { (key, value) ->
        key.urlEncoded() + "=" + value.joinToString(",") { it.urlEncoded() }
    }

    else -> entries.flatMap { (key, values) ->
        values.map { key to it }
    }.joinToString("&") { (key, value) -> "${key.urlEncoded()}=${value.urlEncoded()}" }
}

/** Read url parameters from string */
fun urlParametersFrom(string: String): UrlParameters = mutableUrlParametersFrom(string)

/**
 *
 * Mutable UrlParameters
 *
 */
typealias MutableUrlParameters = MutableMap<String, MutableList<String>>

fun mutableUrlParametersOf(vararg pair: Pair<String, List<String>>) = pair.associate { it.first to it.first.toMutableList() }.toMutableMap()

fun mutableUrlParametersOf(): MutableUrlParameters = mutableMapOf()

fun UrlParameters.toMutableUrlParameters(): MutableUrlParameters = mapValues { it.value.toMutableList() }.toMutableMap()

/** Read url parameters from string and returns a mutable parameter collection */
fun mutableUrlParametersFrom(string: String): Map<String, List<String>> = if (string.isBlank()) emptyMap() else {
    val fields = string.split("&")
    mutableMapOf<String, MutableList<String>>().also { map ->
        fields.mapNotNull { field ->
            val (key, values) = field.splitToPairLax('=') ?: return@mapNotNull null
            key to if (values.contains(",")) values.split(",").map { decodeUrlEncoded(it) }
            else listOf(values)
        }.forEach { (key, values) ->
            val list = map.computeIfAbsent(key) { mutableListOf() }
            list.addAll(values)
        }
    }
}

/* String collection utils */


@Deprecated("Deprecated", ReplaceWith("toUrlEncodedList()"))
fun Collection<String>.urlEncoded() = toUrlEncodedList()

/** convert a string list to url-encoded string list.
 * A string that containing all url-encoded elements separated by ','
 */
fun Collection<String>.toUrlEncodedList() = joinToString(",") { it.urlEncoded() }

/** parse a url encoded */
fun urlEncodedListFrom(string: String): List<String> = if (string.isBlank()) emptyList() else string.split(",").map { decodeUrlEncoded(it) }

/** parse a url encoded list and put all values to set */
fun urlEncodedSetFrom(string: String): Set<String> = if (string.isBlank()) emptySet() else urlEncodedListFrom(string).toSet()

/* String map utils */

@Deprecated("Deprecated", ReplaceWith("toUrlEncodedMap()"))
fun Map<String, String>.urlEncoded() = toUrlEncodedMap()

/** Convert a string-to-string map to url-encoded form */
fun Map<String, String>.toUrlEncodedMap() = if (isEmpty()) "" else entries.joinToString("&") { (key, value) ->
    """${key.urlEncoded()}=${value.urlEncoded()}"""
}

/** build a string-to-string map from url-encoded string */
fun urlEncodedMapFrom(string: String) = if (string.isBlank()) emptyMap() else string.split("&")
    .mapNotNull { str -> str.takeIf { it.isNotBlank() }?.splitToPairLax('=') }
    .toMap()
