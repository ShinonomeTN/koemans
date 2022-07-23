package com.shinonometn.koemans.utils

/**
 * Take a value, return null if catch any exception.
 */
fun <T> nullIfFailed(provider : () -> T) : T? {
    return try {
        provider()
    } catch (e : Exception) {
        null
    }
}

/**
 * Get a result from provider
 */
fun <T> resultOf(provider: () -> T) : Result<T> {
    return try {
        Result.success(provider())
    } catch (e: Exception) {
        Result.failure(e)
    }
}