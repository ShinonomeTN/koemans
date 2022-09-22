package com.shinonometn.koemans.utils

/**
 * Get stack trace strings that matching predicate
 */
fun Exception.stackTraceLines(filter : (StackTraceElement) -> Boolean) : List<String> {
    val stackTraceElements = stackTrace ?: return emptyList()
    return stackTraceElements.filter(filter).map { it.simpleStringRepresent() }
}

/**
 * Get stack trace strings
 */
fun Exception.stackTraceLines() = stackTraceLines { true }

/**
 * Get stack trace strings in given deep
 */
fun Exception.stackTraceLines(deep : Int) : List<String> {
    val stackTraceElement = stackTrace ?: return emptyList()
    return stackTraceElement.take(deep).map { it.simpleStringRepresent() }
}

/**
 * Get stack trace string from StackTraceElement
 */
fun StackTraceElement.simpleStringRepresent() = "${className}.${methodName}@${fileName}:${lineNumber}"

/**
 * Get first stack trace element string
 */
fun Exception.firstCauseString() : String? {
    return stackTrace.firstOrNull()?.simpleStringRepresent()
}

/** Throw an IllegalArgumentException with optional [message] */
fun illegalArgument(message : String? = null) : Nothing = throw IllegalArgumentException(message)

/** Throw an IllegalArgumentException with message provider  */
fun illegalArgument(message : () -> String?): Nothing = illegalArgument(message())