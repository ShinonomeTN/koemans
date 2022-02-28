package com.shinonometn.koemans.eventhub

import com.shinonometn.koemans.eventhub.message.MessageComposeMethod
import com.shinonometn.koemans.eventhub.message.SimpleMessagePayload
import com.shinonometn.koemans.eventhub.message.SimpleMessagePayloadComposer
import com.shinonometn.koemans.eventhub.message.new
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

private typealias Composer<T> = SimpleMessagePayloadComposer<T>
private typealias Payload<T> = SimpleMessagePayload<T>

class SimpleMessageEventHub(
    override val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Unconfined)
) : SimpleEventHub<Composer<*>, Payload<*>>() {

    @Suppress("UNCHECKED_CAST")
    fun <TContext> subscribe(
        topic: Composer<TContext>,
        block: SimpleEventHubListener<Composer<TContext>, Payload<TContext>>
    ): SimpleEventHubListener<Composer<*>, Payload<*>> {
        return addListener(topic, block as SimpleEventHubListener<Composer<*>, Payload<*>>)
    }

    fun <TContext> emit(source: Any?, topic: Composer<TContext>, configure: MessageComposeMethod<TContext, Payload<TContext>>? = null): Job? {
        return awaitableEmit(source ?: Unit, topic, configure?.let { topic.new(it) } ?: topic.constructMessage())
    }
}