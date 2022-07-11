package com.shinonometn.koemans.utils

fun Exception.stackTraceLines(filter : (StackTraceElement) -> Boolean) : List<String> {
    val stackTraceElements = stackTrace ?: return emptyList()
    return stackTraceElements.filter(filter).map { it.simpleStringRepresent() }
}

fun Exception.stackTraceLines() = stackTraceLines { true }

fun Exception.stackTraceLines(count : Int) : List<String> {
    val stackTraceElement = stackTrace ?: return emptyList()
    return stackTraceElement.take(count).map { it.simpleStringRepresent() }
}

fun StackTraceElement.simpleStringRepresent() = "${className}.${methodName}@${fileName}:${lineNumber}"

fun Exception.firstCauseString() : String? {
    return stackTrace.firstOrNull()?.simpleStringRepresent()
}