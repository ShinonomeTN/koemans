package com.shinonometn.koemans.utils

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Convert a long to local datetime
 */
fun Long.toLocalDateTime(zoneId : ZoneId = ZoneId.systemDefault()): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(this), zoneId)