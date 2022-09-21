package com.shinonometn.koemans.utils

import java.io.File
import java.net.URI
import java.net.URL
import java.nio.file.Path

private val normalPathPattern = Regex("^([^:?]+)(?:\\?.*?)?$")
private val urlBaseNamePattern = Regex("^(?:[^/:]+?:/{0,2}?)([^?]+)*?(?:\\?.*)?$")

internal fun resolvePathBaseName(string : String) : String {
    val normalPathMath = normalPathPattern.matchEntire(string)
    if(normalPathMath != null) return normalPathMath
        .groupValues[1].removeSurrounding("/").split("/").last()

    return (urlBaseNamePattern.matchEntire(string)
        ?.groupValues?.last()?.removePrefix("//")?.removeSurrounding("/")
        ?: "").split("/").last()
}

fun URL.resolveBaseName()  = resolvePathBaseName(toString())

fun URI.resolveBaseName()  = resolvePathBaseName(toString())

fun Path.resolveBaseName()  = resolvePathBaseName(toString())

fun File.resolveBaseName()  = resolvePathBaseName(path)