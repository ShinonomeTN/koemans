package com.shinonometn.koemans.utils

import org.junit.Test

import org.slf4j.LoggerFactory
import java.util.Date

class DateTimeUtilsKtTest {
    private val logger = LoggerFactory.getLogger(DateTimeUtilsKtTest::class.java)

    @Test
    fun toLocalDateTime() {
        val current = System.currentTimeMillis()
        logger.info("Current millis: {}, Datetime: {}", current, current.toLocalDateTime())
    }

    @Test
    fun testToLocalDateTime() {
        val current = Date()
        logger.info("Current date: {}, Datetime: {}", current, current.toLocalDateTime())
    }
}