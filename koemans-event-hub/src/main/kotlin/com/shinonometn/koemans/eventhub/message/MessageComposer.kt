package com.shinonometn.koemans.eventhub.message

interface MessageComposer<TContext, TMessage> {
    fun constructMessage(): TMessage
}

typealias MessageComposeMethod<TContext, TMessage> = MessageComposer<TContext, TMessage>.(TMessage) -> Unit

inline fun <TContext,TMessage> MessageComposer<TContext, TMessage>.new(crossinline block: MessageComposeMethod<TContext, TMessage>): TMessage {
    return constructMessage().also { e -> block.invoke(this, e) }
}