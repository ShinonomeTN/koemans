package com.shinonometn.koemans.utils

import junit.framework.TestCase
import org.junit.Test
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

class TimeUtilsKtTest : TestCase() {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Test
    fun testToDuration() {
        logger.info("1 hour (h1) to duration is: {}", "1h".toDuration())
    }

    @Test
    fun testToLocalDateTime() {
        val now = LocalDateTime.now()
        val formattedNow = now.format()
        logger.info("Now is : {}. reformatted: {}", formattedNow, formattedNow.toLocalDateTime())
    }
}