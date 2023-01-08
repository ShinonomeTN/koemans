package com.shinonometn.koemans.utils

/**
 * Take a value, return null if catch any exception.
 */
fun <T> successOrNull(provider: () -> T): T? {
    return try {
        provider()
    } catch (e: Exception) {
        null
    }
}

/**
 * Get a result from provider
 */
fun <T> resultOf(provider: () -> T): Result<T> {
    return try {
        Result.success(provider())
    } catch (e: Exception) {
        Result.failure(e)
    }
}

/** Throw an illegal state exception with optional message and reason */
fun illegalState(message: String? = null, cause: Throwable? = null): Nothing = throw IllegalStateException(message, cause)

/** Throw an illegal argument exception with optional message and reason */
fun illegalArgument(message: String? = null, cause: Throwable? = null): Nothing = throw IllegalArgumentException(message, cause)

/**
 * Take if type match
 */
inline fun <reified T> Any.takeIfIs(): T? = if (this is T) this else null

/** Give some altering if matching condition */
fun <T> T.transformIf(condition: (T) -> Boolean = { true }, transform: (T) -> T) = if (condition(this)) transform(this) else this


/** Give some altering if matching condition */
fun <T> T.transformIf(condition: Boolean, transform: (T) -> T) = if (condition) transform(this) else this

/** Pair plus one element is a triple */
infix fun <A, B, C> Pair<A, B>.to(any: C) = Triple(first, second, any)

operator fun <A, B, C> Pair<A, B>.plus(any: C) = to(any)

infix fun <A, B, C> C.to(pair: Pair<A, B>) = Triple(this, pair.first, pair.second)

operator fun <A, B, C> C.plus(pair: Pair<A, B>) = to(pair)

/** Four elements in a group is a quadruple */
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val forth: D)

operator fun <A, B, C, D> Triple<A, B, C>.plus(any: D) = to(any)

infix fun <A, B, C, D> Triple<A, B, C>.to(any: D) = Quadruple(first, second, third, any)

infix fun <A, B, C, D> D.to(triple: Triple<A, B, C>) = Quadruple(this, triple.first, triple.second, triple.third)

operator fun <A, B, C, D> D.plus(triple: Triple<A, B, C>) = to(triple)

fun <A, B, C, D> Pair<A, B>.to(pair: Pair<C, D>) = Quadruple(first, second, pair.first, pair.second)

operator fun <A, B, C, D> Pair<A, B>.plus(another: Pair<C, D>) = to(another)

/** Five elements in a group is a quintuple */
data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val forth: D, val fifth: E)

infix fun <A, B, C, D, E> Quadruple<A, B, C, D>.to(any: E) = Quintuple(first, second, third, forth, any)

operator fun <A, B, C, D, E> Quadruple<A, B, C, D>.plus(any: E) = to(any)

operator fun <A, B, C, D, E> E.plus(quadruple: Quadruple<A, B, C, D>) = to(quadruple)

infix fun <A, B, C, D, E> E.to(quadruple: Quadruple<A, B, C, D>) =
    Quintuple(this, quadruple.first, quadruple.second, quadruple.third, quadruple.forth)

/** Six elements in a group is a sextuple */
data class Sextuple<A, B, C, D, E, F>(val first: A, val second: B, val third: C, val forth: D, val fifth: E, val sixth: F)

infix fun <A, B, C, D, E, F> Quintuple<A, B, C, D, E>.to(any: F) = Sextuple(first, second, third, forth, fifth, any)

operator fun <A, B, C, D, E, F> Quintuple<A, B, C, D, E>.plus(any: F) = to(any)

operator fun <A, B, C, D, E, F> F.plus(quintuple: Quintuple<A, B, C, D, E>) = to(quintuple)

infix fun <A, B, C, D, E, F> F.to(quintuple: Quintuple<A, B, C, D, E>) =
    Sextuple(this, quintuple.first, quintuple.second, quintuple.third, quintuple.forth, quintuple.fifth)

/** Seven elements in a group is a septuple */

data class Septuple<A, B, C, D, E, F, G>(val first: A, val second: B, val third: C, val forth: D, val fifth: E, val sixth: F, val seventh: G)

infix fun <A, B, C, D, E, F, G> Sextuple<A, B, C, D, E, F>.to(any: G) = Septuple(first, second, third, forth, fifth, sixth, any)

operator fun <A, B, C, D, E, F, G> Sextuple<A, B, C, D, E, F>.plus(any: G) = to(any)

infix fun <A, B, C, D, E, F, G> G.to(sextuple: Sextuple<A, B, C, D, E, F>) =
    Septuple(this, sextuple.first, sextuple.second, sextuple.forth, sextuple.fifth, sextuple.sixth, sextuple)

operator fun <A, B, C, D, E, F, G> G.plus(sextuple: Sextuple<A, B, C, D, E, F>) = to(sextuple)

/** Eight elements in a group is a octuple */
data class Octuple<A, B, C, D, E, F, G, H>(
    val first: A,
    val second: B,
    val third: C,
    val forth: D,
    val fifth: E,
    val sixth: F,
    val seventh: G,
    val eighth: H
)

infix fun <A, B, C, D, E, F, G, H> Septuple<A, B, C, D, E, F, G>.to(any: H) = Octuple(first, second, third, forth, fifth, sixth, seventh, any)

operator fun <A, B, C, D, E, F, G, H> Septuple<A, B, C, D, E, F, G>.plus(any: H) = to(any)

infix fun <A, B, C, D, E, F, G, H> H.to(septuple: Septuple<A, B, C, D, E, F, G>) =
    Octuple(this, septuple.first, septuple.second, septuple.third, septuple.forth, septuple.fifth, septuple.sixth, septuple.seventh)

operator fun <A, B, C, D, E, F, G, H> H.plus(septuple: Septuple<A, B, C, D, E, F, G>) = to(septuple)

/** We don't have nine elements in a group :) */