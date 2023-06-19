package com.shinonometn.koemans.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Convert a long to local datetime
 */
fun Long.toLocalDateTime(zoneId : ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)

/**
 * Convert a Date to local datetime
 */
fun Date.toLocalDateTime(zoneId: ZoneId = ZoneId.systemDefault()) : LocalDateTime =
    LocalDateTime.ofInstant(toInstant(), zoneId)