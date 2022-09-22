package com.shinonometn.koemans.utils

import org.junit.Test
import org.slf4j.LoggerFactory

class LangKtTest {
    private val logger = LoggerFactory.getLogger(LangKtTest::class.java)

    @Test
    fun `Test grouping 1`() {
        val sb = StringBuilder("(Any() to Any())")
        (Any() to Any()).also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" + Any()"); it + Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }
    }

    @Test
    fun `Test grouping 2`() {
        val sb = StringBuilder("Any() to Any()")
        (Any() to Any()).also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }.let { sb.append(" to Any()"); it to Any() }.also {
            logger.debug("$sb is a ${it::class.simpleName}.")
        }
    }
}