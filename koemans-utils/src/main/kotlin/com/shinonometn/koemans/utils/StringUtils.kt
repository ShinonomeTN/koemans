package com.shinonometn.koemans.utils

/**
 * Copy from io.ktor.server.engine.CommandLine
 *
 * Split string with first [separator] meet
 * If no separator, return null
 */
fun String.splitToPair(separator: Char): Pair<String, String>? = indexOf(separator).let { idx ->
    when (idx) {
        -1 -> null
        else -> Pair(take(idx), drop(idx + 1))
    }
}

/**
 * Check if string is a literal number
 */
fun String.isNumber(): Boolean {
    if (isBlank()) return false
    for (c in this) {
        if (!c.isDigit()) return false
    }

    return true
}

/**
 * Check if string is a literal decimal
 *
 * @param maxFloatDigests set the length limitation of the float part, default is '18'.
 * @param allowLeftPaddingZeros set if allow left padding zeros, default is 'false'.
 */
fun String.isDecimal(maxFloatDigests: Int = 18, allowLeftPaddingZeros: Boolean = false): Boolean {
    if (isBlank()) return false

    if (!allowLeftPaddingZeros && length > 2) when (this[0]) {
        '+', '-' -> if (this[1] == '0' && this.length > 2 && this[2] == '0') return false
        '0' -> if (this[1] == '0') return false
    }

    var decimalMode = false

    var symbolCount = 0

    var intPartCount = 0
    var digestCountdown = maxFloatDigests

    for (c in this) if (decimalMode) when {
        !c.isDigit() || digestCountdown <= 0 -> return false
        else -> digestCountdown--
    } else when {
        (c == '+' || c == '-') && (symbolCount == 0) -> {
            symbolCount++; continue
        }
        c == '.' -> {
            decimalMode = true; continue
        }
        !c.isDigit() -> return false
        else -> {
            intPartCount++; continue
        }
    }

    return when {
        intPartCount == 0 -> false
        digestCountdown == maxFloatDigests && decimalMode -> false
        else -> true
    }
}

/**
 * Check if string is a literal boolean
 */
fun String.isBoolean(): Boolean {
    val l = toLowerCase()
    return l == "true" || l == "false"
}

fun secondsToNiceTimeString(s: Long): String {
    return "${s / 86400}d ${s / 3600 % 24}h ${s / 60 % 60}m ${s % 60}s"
}