package com.shinonometn.koemans.eventhub

import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

typealias SimpleEventHubListener<TTopic, TPayload> = suspend TTopic.(source: Any, message: TPayload) -> Unit

/**
 * A simple event hub implementation
 */
open class SimpleEventHub<TTopic, TPayload> : EventHub<TTopic, TPayload, SimpleEventHubListener<TTopic, TPayload>> {

    /**
     * Logger provider
     *
     * Override this to use different logger
     */
    protected open val logger: Logger = LoggerFactory.getLogger("SimpleEventHub")

    /**
     * Listener registry
     *
     * Override this to use different registry implement
     */
    protected open val listenerRegistry: MutableMap<TTopic, MutableList<SimpleEventHubListener<TTopic, TPayload>>> by lazy {
        ConcurrentHashMap()
    }

    fun currentRegistryStatus(): Map<String, Int> {
        return listenerRegistry.map { it.key.toString() to it.value.count() }.toMap()
    }

    /**
     * Coroutine scope for message event handling
     *
     * Override this to use different coroutine scope
     */
    open val coroutineScope: CoroutineScope by lazy { CoroutineScope(Dispatchers.IO) }

    override fun emit(source: Any, topic: TTopic, message: TPayload) {
        awaitableEmit(source, topic, message)
    }

    override fun addListener(topic: TTopic, listener: SimpleEventHubListener<TTopic, TPayload>): SimpleEventHubListener<TTopic, TPayload> {
        listenerRegistry.computeIfAbsent(topic) { LinkedList() }.add(listener)
        return listener
    }

    override fun removeListener(topic: TTopic, listener: SimpleEventHubListener<TTopic, TPayload>): SimpleEventHubListener<TTopic, TPayload> {
        listenerRegistry[topic]?.remove(listener)
        return listener
    }

    /**
     * Emit an message, return the notification job if there are any listener
     */
    fun awaitableEmit(source: Any, topic: TTopic, message: TPayload): Job? {
        val listeners = listenerRegistry[topic]?.takeIf { it.isNotEmpty() } ?: return null.also {
            logger.debug("No listener for message type {}", topic)
        }

        return coroutineScope.launch {
            listeners.map {
                async {
                    try {
                        it.invoke(topic, source, message)
                    } catch (e: Exception) {
                        logger.warn(
                            "Message handler raised and exception. source '{}', topic '{}', message '{}', error '{}'.",
                            source, topic, message, e.stackTraceToString()
                        )
                    }
                }
            }.awaitAll()
        }
    }
}