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

/**
 * Take if type match
 */
inline fun <reified T> Any.takeIfIs(): T? {
    return if (this is T) this else null
}

/** Give some altering if matching condition */
fun <T> T.transformIf(condition: (T) -> Boolean = { true }, transform: (T) -> T) = if (condition(this)) transform(this) else this

/** Pair plus one element is a triple */
operator fun <A, B, C> Pair<A, B>.plus(any: C) = Triple(first, second, any)

operator fun <A, B, C> C.plus(pair: Pair<A, B>) = Triple(this, pair.first, pair.second)

/** Four elements in a group is a quadruple */
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val forth: D)

operator fun <A, B, C, D> Triple<A, B, C>.plus(any: D) = Quadruple(first, second, third, any)

operator fun <A, B, C, D> D.plus(triple: Triple<A, B, C>) = Quadruple(this, triple.first, triple.second, triple.third)

fun <A, B, C, D> Pair<Pair<A, B>, Pair<C, D>>.toQuadruple() = Quadruple(first.first, first.second, second.first, second.second)

operator fun <A, B, C, D> Pair<A, B>.plus(another: Pair<C, D>) = Quadruple(first, second, another.first, another.second)

/** Five elements in a group is a quintuple */
data class Quintuple<A, B, C, D, E>(val first: A, val second: B, val third: C, val forth: D, val fifth: E)

operator fun <A, B, C, D, E> Quadruple<A, B, C, D>.plus(any: E) = Quintuple(first, second, third, forth, any)

operator fun <A, B, C, D, E> E.plus(quadruple: Quadruple<A, B, C, D>) =
    Quintuple(this, quadruple.first, quadruple.second, quadruple.third, quadruple.forth)

/** Six elements in a group is a sextuple */
data class Sextuple<A, B, C, D, E, F>(val first: A, val second: B, val third: C, val forth: D, val fifth: E, val sixth: F)

operator fun <A, B, C, D, E, F> Quintuple<A, B, C, D, E>.plus(any: F) = Sextuple(first, second, third, forth, fifth, any)

operator fun <A, B, C, D, E, F> F.plus(any: Quintuple<A, B, C, D, E>) = Sextuple(this, any.first, any.second, any.third, any.forth, any.fifth)

/** Seven elements in a group is a septuple */

data class Septuple<A, B, C, D, E, F, G>(val first: A, val second: B, val third: C, val forth: D, val fifth: E, val sixth: F, val seventh: G)

operator fun <A, B, C, D, E, F, G> Sextuple<A, B, C, D, E, F>.plus(any: G) = Septuple(first, second, third, forth, fifth, sixth, any)

operator fun <A, B, C, D, E, F, G> G.plus(sextuple: Sextuple<A, B, C, D, E, F>) =
    Septuple(this, sextuple.first, sextuple.second, sextuple.forth, sextuple.fifth, sextuple.sixth, sextuple)

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

operator fun <A, B, C, D, E, F, G, H> Septuple<A, B, C, D, E, F, G>.plus(any: H) = Octuple(first, second, third, forth, fifth, sixth, seventh, any)

operator fun <A, B, C, D, E, F, G, H> H.plus(any: Septuple<A, B, C, D, E, F, G>) =
    Octuple(this, any.first, any.second, any.third, any.forth, any.fifth, any.sixth, any.seventh)

/** We don't have nine elements in a group :) */