package com.shinonometn.koemans.eventhub

import com.shinonometn.koemans.eventhub.SimpleMessageEventHubTest.SimpleMessage.text
import com.shinonometn.koemans.eventhub.message.SimpleMessagePayload
import com.shinonometn.koemans.eventhub.message.SimpleMessagePayloadComposer
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.slf4j.LoggerFactory

class SimpleMessageEventHubTest {

    private val eventHub = SimpleMessageEventHub()

    private val logger = LoggerFactory.getLogger("SimpleMessageEventHubTest")

    object SimpleMessage : SimpleMessagePayloadComposer<SimpleMessage>() {
        var SimpleMessagePayload<SimpleMessage>.text : String
            get() = readPayload("text")
            set(value) = writePayload("text", value)
    }

    @Test
    fun `Test message emit`() {
        val list = mutableListOf<String>()

        runBlocking {
            eventHub.subscribe(SimpleMessage) { source, payload ->
                logger.info("SimpleMessage.listener1, source: '{}', payload.text: '{}'.", source, payload.text)
                list.add(payload.text)
            }

            eventHub.emit(this, SimpleMessage) {
                it.text = "Hello world"
            }

            delay(1000)
        }

        assertTrue("Should have one item.", list.size == 1 && list.first() == "Hello world")
    }
}