package com.shinonometn.koemans.web.session

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.sessions.*
import io.ktor.util.*
import kotlin.reflect.KClass

class KoemansSessions(val providers: List<KoemansSessionProvider<*>>) {
    class Configuration {
        private val registered = mutableListOf<KoemansSessionProvider<*>>()
        val providers: List<KoemansSessionProvider<*>> get() = registered

        fun register(provider: KoemansSessionProvider<*>) {
            registered.firstOrNull { (it.name == provider.name) || (it.type == provider.type) }
                ?.let { throw IllegalArgumentException("Session provider with name ${provider.name} already registered: $it") }

            registered.add(provider)
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, KoemansSessions> {
        override val key: AttributeKey<KoemansSessions> = AttributeKey("KoemansSession")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): KoemansSessions {
            val config = Configuration().apply(configure)
            val feature = KoemansSessions(config.providers)

            pipeline.intercept(ApplicationCallPipeline.Features) {
                val providedData = feature.providers.associateBy({ it.name }) { it.receiveSessionData(call) }
                val sessionData = RequestSessionInfo(feature, providedData)
                call.attributes.put(SessionKey, sessionData)
            }

            pipeline.sendPipeline.intercept(ApplicationSendPipeline.Before) {
                val sessions = call.attributes.getOrNull(SessionKey) ?: return@intercept

                sessions.providedData.values.forEach { data ->
                    data.sendSessionData(call)
                }

                sessions.commit()
            }

            return feature
        }

    }
}

/**
 *  Session data provider
 * */
class KoemansSessionProvider<TSessionBean : Any>(
    val name: String,
    val type: KClass<TSessionBean>,
    val transport: SessionTransport,
    val tracker: SessionTracker<TSessionBean>
) {
    override fun toString(): String {
        return "KoemansSessionProvider(name='$name', type=$type, transport=$transport, tracker=$tracker)"
    }
}

/**
 * Helper function for receiving a session from the request.
 * */
private suspend fun <TSessionBean : Any> KoemansSessionProvider<TSessionBean>.receiveSessionData(
    call: ApplicationCall
): SessionProviderData<TSessionBean> {
    val receivedValue = transport.receive(call)
    val extracted = tracker.load(call, receivedValue)
    val incoming = (receivedValue != null) || (extracted != null)
    return SessionProviderData(extracted, incoming, this)
}

/**
 * Bean that holding information and value of a session item
 * */
private class SessionProviderData<TSessionBean : Any>(
    var value: TSessionBean?,
    val incoming: Boolean,
    val provider: KoemansSessionProvider<TSessionBean>,
    var isChanged: Boolean = false
)

/**
 * Helper function for sending session data to client
 * */
private suspend fun <TSessionBean : Any> SessionProviderData<TSessionBean>.sendSessionData(
    call: ApplicationCall
) {
    val value = value
    val isChanged = isChanged
    when {
        value != null -> if (isChanged) {
            val wrapped = provider.tracker.store(call, value)
            provider.transport.send(call, wrapped)
        }
        incoming -> {
            provider.transport.clear(call)
            provider.tracker.clear(call)
        }
    }
}

/**
 * Session info storage of current request
 */
interface CurrentKoemansSession {
    fun set(name: String, value: Any?)

    fun get(name: String): Any?

    fun clear(name: String)

    fun findNameOf(type: KClass<*>): String
}

inline fun <reified T> CurrentKoemansSession.set(value: T?) {
    set(findNameOf(T::class), value)
}

inline fun <reified T> CurrentKoemansSession.get(): T? {
    return get(findNameOf(T::class)) as? T
}

inline fun <reified T> CurrentKoemansSession.clear() {
    clear(findNameOf(T::class))
}

inline fun <reified T> CurrentKoemansSession.getOrSet(
    name: String = findNameOf(T::class),
    generator: () -> T
): T {
    return get<T>() ?: generator().apply { set(name, this) }
}

private class RequestSessionInfo(
    // Information of all installed session providers
    val sessionFeature: KoemansSessions,
    // Store fetched session data from the current request
    val providedData: Map<String, SessionProviderData<*>>
) : CurrentKoemansSession {
    private var committed = false

    // Preventing changes after send
    fun commit() {
        committed = true
    }

    override fun set(name: String, value: Any?) {
        if (committed) throw IllegalStateException("Session is already committed")
        val providedData = providedData[name] ?: throw IllegalArgumentException("No session named '$name'")

        setTyped(providedData, value)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <S : Any> setTyped(data: SessionProviderData<S>, value: Any?) {
        if (value != null) {
            data.provider.tracker.validate(value as S)
        }
        data.value = value as S
        data.isChanged = true
    }

    override fun get(name: String): Any? {
        val providedData = providedData[name] ?: throw IllegalArgumentException("No session named '$name'")
        return providedData.value
    }

    override fun clear(name: String) {
        val providerData = providedData[name] ?: throw IllegalArgumentException("No session named '$name'")
        providerData.value = null
        providerData.isChanged = true
    }

    override fun findNameOf(type: KClass<*>): String {
        val entry = providedData.entries.firstOrNull { it.value.provider.type == type }
            ?: throw IllegalArgumentException("No session of type '${type.simpleName}'")

        return entry.value.provider.name
    }
}

/**
 * Get current session or fail if no KoemansSession feature installed
 * */
val ApplicationCall.kSession: CurrentKoemansSession
    get() = attributes.getOrNull(SessionKey) ?: reportMissingSession()

private fun ApplicationCall.reportMissingSession(): Nothing {
    application.feature(KoemansSessions)
    throw IllegalStateException("KoemansSession is not configured.")
}

private val SessionKey = AttributeKey<RequestSessionInfo>("KoemansSessionKey")