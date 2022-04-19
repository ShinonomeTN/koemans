package com.shinonometn.koemans.utils

import java.net.URLDecoder
import java.net.URLEncoder

@Suppress("unused")
class UrlQueryParameter constructor() : HashMap<String, MutableCollection<String>>() {

    var keyCodec = UTF8UrlCodec
    var valueCodec = UTF8UrlCodec

    interface Codec {
        fun toValue(string: String) : String
        fun fromValue(string: String) : String
    }

    companion object {
        val UTF8UrlCodec = object : Codec {
            override fun toValue(string: String) = URLEncoder.encode(string, "UTF-8")
            override fun fromValue(string: String) = URLDecoder.decode(string, "UTF-8")
        }

        fun fromString(string: String, codec : Codec = UTF8UrlCodec) = fromString(string, codec, codec)

        fun fromString(string: String, keyCodec : Codec = UTF8UrlCodec, valueCodec: Codec = UTF8UrlCodec): UrlQueryParameter {
            val urlQueryParameter = UrlQueryParameter()
            urlQueryParameter.keyCodec = keyCodec
            urlQueryParameter.valueCodec = valueCodec
            string.split("&").forEach { field ->
                val (key, value) = field.separate()
                value.split(",").forEach {
                    urlQueryParameter.append(keyCodec.fromValue(key), valueCodec.fromValue(it))
                }
            }
            return urlQueryParameter
        }

        private fun String.separate() : Pair<String, String> {
            return indexOf("=").let { idx ->
                when (idx) {
                    -1 -> this to ""
                    else -> Pair(take(idx), drop(idx + 1))
                }
            }
        }
    }

    constructor(map : Map<String, Collection<String>>) : this() {
        map.entries.forEach {
            put(it.key, it.value.toMutableList())
        }
    }

    constructor(builder : UrlQueryParameter.() -> Unit) : this() {
        this.builder()
    }

    fun append(name : String, value : String) {
        val list = getOrPut(name) { mutableListOf() }
        list.add(value)
    }

    fun remove(name : String, value : String) {
        val list = get(name) ?: return
        list.removeIf { it == value }
    }

    fun toUrlEncoded(flattenList : Boolean = true) : String {
        return if(flattenList) entries.flatMap { e -> e.value.map { e.key to it } }.joinToString("&") {
            "${keyCodec.toValue(it.first)}=${valueCodec.toValue(it.second)}"
        } else entries.joinToString("&") {
            "${keyCodec.toValue(it.key)}=" + it.value.joinToString(",") { valueCodec.toValue(it) }
        }
    }
}