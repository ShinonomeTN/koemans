package com.shinonometn.koemans.web.session

import io.ktor.sessions.*
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType

/**
 * Helper
 * */

@PublishedApi
internal inline fun <reified T> typeOf() = T::class.createType()

/**
 * Configure sessions to get it from cookie using session [storage]
 */
inline fun <reified S : Any> KoemansSessions.Configuration.cookie(name: String, storage: SessionStorage) {
    val sessionType = S::class

    val builder = CookieIdSessionBuilder(sessionType, typeOf<S>())
    cookie(name, builder, sessionType, storage)
}

@PublishedApi
internal fun <S : Any> KoemansSessions.Configuration.cookie(
    name: String,
    builder: CookieIdSessionBuilder<S>,
    sessionType: KClass<S>,
    storage: SessionStorage
) {
    val transport = SessionTransportCookie(name, builder.cookie, builder.transformers)
    val tracker = SessionTrackerById(sessionType, builder.serializer, storage, builder.sessionIdProvider)
    val provider = KoemansSessionProvider(name, sessionType, transport, tracker)
    register(provider)
}

/**
 * Configures a session using a cookie with the specified [name] using it as a session id.
 * The actual content of the session is stored at server side using the specified [storage].
 * The cookie configuration can be set inside [block] using the cookie property exposed by [CookieIdSessionBuilder].
 */
inline fun <reified S : Any> KoemansSessions.Configuration.cookie(
    name: String,
    storage: SessionStorage,
    block: CookieIdSessionBuilder<S>.() -> Unit
) {
    val sessionType = S::class

    val builder = CookieIdSessionBuilder(sessionType, typeOf<S>()).apply(block)
    cookie(name, builder, sessionType, storage)
}

/**
 * Configure sessions to get it from HTTP header using session [storage]
 */
inline fun <reified S : Any> KoemansSessions.Configuration.header(name: String, storage: SessionStorage) {
    header<S>(name, storage, {})
}

/**
 * Configures a session using a header with the specified [name] using it as a session id.
 * The actual content of the session is stored at server side using the specified [storage].
 */
inline fun <reified S : Any> KoemansSessions.Configuration.header(
    name: String,
    storage: SessionStorage,
    block: HeaderIdSessionBuilder<S>.() -> Unit
) {
    val sessionType = S::class

    val builder = HeaderIdSessionBuilder(sessionType, typeOf<S>()).apply(block)
    header(name, sessionType, storage, builder)
}

@PublishedApi
internal fun <S : Any> KoemansSessions.Configuration.header(
    name: String,
    sessionType: KClass<S>,
    storage: SessionStorage?,
    builder: HeaderSessionBuilder<S>
) {
    val transport = SessionTransportHeader(name, builder.transformers)
    val tracker = when {
        storage != null && builder is HeaderIdSessionBuilder<S> -> SessionTrackerById(
            sessionType,
            builder.serializer,
            storage,
            builder.sessionIdProvider
        )
        else -> SessionTrackerByValue(sessionType, builder.serializer)
    }
    val provider = KoemansSessionProvider(name, sessionType, transport, tracker)
    register(provider)
}

// cookie by value

/**
 * Configure sessions to serialize to/from HTTP cookie
 */
inline fun <reified S : Any> KoemansSessions.Configuration.cookie(name: String) {
    val sessionType = S::class

    val builder = CookieSessionBuilder(sessionType, typeOf<S>())
    cookie(name, sessionType, builder)
}

/**
 * Configures a session using a cookie with the specified [name] using it as for the actual session content
 * optionally transformed by specified transforms in [block].
 * The cookie configuration can be set inside [block] using the cookie property exposed by [CookieIdSessionBuilder].
 */
inline fun <reified S : Any> KoemansSessions.Configuration.cookie(
    name: String,
    block: CookieSessionBuilder<S>.() -> Unit
) {
    val sessionType = S::class

    val builder = CookieSessionBuilder(sessionType, typeOf<S>()).apply(block)
    cookie(name, sessionType, builder)
}

@PublishedApi
internal fun <S : Any> KoemansSessions.Configuration.cookie(
    name: String,
    sessionType: KClass<S>,
    builder: CookieSessionBuilder<S>
) {
    val transport = SessionTransportCookie(name, builder.cookie, builder.transformers)
    val tracker = SessionTrackerByValue(sessionType, builder.serializer)
    val provider = KoemansSessionProvider(name, sessionType, transport, tracker)
    register(provider)
}

/**
 * Configure sessions to serialize to/from HTTP header
 */
inline fun <reified S : Any> KoemansSessions.Configuration.header(name: String) {
    header<S>(name) {}
}

/**
 * Configures a session using a header with the specified [name] using it for the actual session content
 * optionally transformed by specified transforms in [block].
 */
inline fun <reified S : Any> KoemansSessions.Configuration.header(
    name: String,
    block: HeaderSessionBuilder<S>.() -> Unit
) {
    val sessionType = S::class

    val builder = HeaderSessionBuilder(sessionType, typeOf<S>()).apply(block)
    header(name, sessionType, null, builder)
}

/**
 * Cookie session configuration builder
 */
class CookieIdSessionBuilder<S : Any>
@PublishedApi
internal constructor(
    type: KClass<S>,
    typeInfo: KType
) : CookieSessionBuilder<S>(type, typeInfo) {

    @Deprecated("Use builder functions instead.")
    constructor(type: KClass<S>) : this(type, type.starProjectedType)

    /**
     * Register session ID generation function
     */
    fun identity(f: () -> String) {
        sessionIdProvider = f
    }

    /**
     * Current session ID provider function
     */
    var sessionIdProvider: () -> String = { generateSessionId() }
        private set
}

/**
 * Cookie session configuration builder
 * @property type - session instance type
 */
open class CookieSessionBuilder<S : Any>
@PublishedApi
internal constructor(
    val type: KClass<S>,
    val typeInfo: KType
) {
    @Deprecated("Use builder functions instead.")
    constructor(type: KClass<S>) : this(type, type.starProjectedType)

    /**
     * Session instance serializer
     */
    var serializer: SessionSerializer<S> = defaultSessionSerializer(typeInfo)

    private val _transformers = mutableListOf<SessionTransportTransformer>()

    /**
     * Registered session transformers
     */
    val transformers: List<SessionTransportTransformer> get() = _transformers

    /**
     * Register a session [transformer]. Useful for encryption, signing and so on
     */
    fun transform(transformer: SessionTransportTransformer) {
        _transformers.add(transformer)
    }

    /**
     * Cookie header configuration
     */
    val cookie: CookieConfiguration = CookieConfiguration()
}

/**
 * Header session configuration builder
 * @property type session instance type
 */
open class HeaderSessionBuilder<S : Any>
@PublishedApi
internal constructor(
    val type: KClass<S>,
    val typeInfo: KType
) {

    @Deprecated("Use builder functions instead.")
    constructor(type: KClass<S>) : this(type, type.starProjectedType)

    /**
     * Session instance serializer
     */
    var serializer: SessionSerializer<S> = defaultSessionSerializer(typeInfo)

    private val _transformers = mutableListOf<SessionTransportTransformer>()

    /**
     * Registered session transformers
     */
    val transformers: List<SessionTransportTransformer> get() = _transformers

    /**
     * Register a session [transformer]. Useful for encryption, signing and so on
     */
    fun transform(transformer: SessionTransportTransformer) {
        _transformers.add(transformer)
    }
}

/**
 * Header session configuration builder
 */
class HeaderIdSessionBuilder<S : Any>
@PublishedApi
internal constructor(
    type: KClass<S>,
    typeInfo: KType
) : HeaderSessionBuilder<S>(type, typeInfo) {

    @Deprecated("Use builder functions instead.")
    constructor(type: KClass<S>) : this(type, type.starProjectedType)

    /**
     * Register session ID generation function
     */
    fun identity(f: () -> String) {
        sessionIdProvider = f
    }

    /**
     * Current session ID provider function
     */
    var sessionIdProvider: () -> String = { generateSessionId() }
        private set
}