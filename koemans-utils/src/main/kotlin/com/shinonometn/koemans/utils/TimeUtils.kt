package com.shinonometn.koemans.utils

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.time.Duration

/** Parse a duration from string. Returns a kotlin duration */
fun String.toDuration() = Duration.parse(this)

private val defaultLocalDateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME

/** Parse string using ISO_DATE_TIME format */
fun String.toLocalDateTime(formatter: DateTimeFormatter = defaultLocalDateTimeFormatter): LocalDateTime = LocalDateTime.parse(this, formatter)

/** Parse string using ISO_DATE_TIME format or null if it's invalid */
fun String.toLocalDateTimeOrNull(formatter: DateTimeFormatter = defaultLocalDateTimeFormatter): LocalDateTime? = try {
    LocalDateTime.parse(this, formatter)
} catch (ignored: Exception) {
    null
}

/** Formatting this LocalDateTime using defaultLocalDateTimeFormatter */
fun LocalDateTime.format(): String = format(defaultLocalDateTimeFormatter)