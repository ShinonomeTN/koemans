package com.shinonometn.koemans.eventhub.message

private typealias PayloadContext<TContext> = MessageComposer<TContext, SimpleMessagePayload<TContext>>

class SimpleMessagePayload<TContext> {
    private val payload: MutableMap<String, Any?> by lazy { HashMap() }

    @Suppress("unused")
    val PayloadContext<TContext>.payload: MutableMap<String, Any?>
        get() = this@SimpleMessagePayload.payload

    fun toMap() = payload.toMap()

    inline fun <reified TReturn> PayloadContext<TContext>.readPayload(key: String): TReturn {
        return payload[key] as TReturn
    }

    inline fun <reified TAccept> PayloadContext<TContext>.writePayload(key: String, value: TAccept) {
        payload[key] = value
    }
}

abstract class SimpleMessagePayloadComposer<TContext> : PayloadContext<TContext> {
    override fun constructMessage(): SimpleMessagePayload<TContext> {
        return SimpleMessagePayload()
    }
}
