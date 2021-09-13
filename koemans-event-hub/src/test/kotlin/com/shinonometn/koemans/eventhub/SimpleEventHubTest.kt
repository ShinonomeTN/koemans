package com.shinonometn.koemans.eventhub

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.slf4j.LoggerFactory

class SimpleEventHubTest {

    private val eventHub = SimpleEventHub<String, Map<String, String>>()

    private val logger = LoggerFactory.getLogger("SimpleEventHubTest")

    @Test
    fun `Test event hub emit`() = runBlocking {
        eventHub.addListener("test_1") { source, payload ->
            logger.info("test_1.listener1: source: '{}', message: '{}'.", source, payload)
        }

        eventHub.addListener("test_1") { source, payload ->
            logger.info("test_1.listener2: source: '{}', message: '{}'.", source, payload)
        }

        eventHub.emit(
            this, "test_1", mapOf(
                "message" to "hello world"
            )
        )

        delay(1000)
    }

    @Test
    fun `Test event hub invoke error`() = runBlocking {
        eventHub.addListener("test_1") { _, _ ->
            error("This is an error.")
        }

        eventHub.addListener("test_1") { _, _ ->
            logger.info("No error here.")
        }

        eventHub.addListener("test_1") { _, _ ->
            logger.info("No error here.")
        }

        eventHub.emit(
            this, "test_1", mapOf(
                "message" to "hello world"
            )
        )

        delay(1000)
    }
}