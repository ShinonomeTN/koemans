package com.shinonometn.koemans.eventhub

/**
 * Event hub interface
 *
 * @param TTopic is event hub topic type, String, object etc..
 * @param TEvent is the event type that this event hub type is using
 * @param TListener is event hub listener type, a method a object etc....
 */
interface EventHub<TTopic, TEvent, TListener> {

    fun emit(source : Any, topic: TTopic, message: TEvent)

    fun addListener(topic: TTopic, listener: TListener) : TListener

    fun removeListener(topic: TTopic, listener: TListener) : TListener
}