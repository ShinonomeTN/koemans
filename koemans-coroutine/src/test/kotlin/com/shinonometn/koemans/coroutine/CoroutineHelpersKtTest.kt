package com.shinonometn.koemans.coroutine

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis

class CoroutineHelpersKtTest {

    private val logger = LoggerFactory.getLogger(CoroutineHelpersKtTest::class.java)

    @Test
    fun `Test execute`() = runBlocking {
        val testThreadName = "Test Thread ${System.currentTimeMillis()}"
        logger.info("Test thread name is [$testThreadName]")

        val executor = Executors.newSingleThreadExecutor {
            Thread(it).apply {
                name = testThreadName
                isDaemon = true
            }
        }

        val time = measureTimeMillis {
            val result = background(executor) {
                logger.info("Start sleep 1000ms.")
                Thread.sleep(1000)
                Thread.currentThread().name
            }

            assertEquals(result, testThreadName)
        }

        assertTrue("Sleep time should larger or equals 1000ms",time >= 1000)
    }
}