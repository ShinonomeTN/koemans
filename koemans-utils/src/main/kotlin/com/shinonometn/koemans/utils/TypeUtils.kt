package com.shinonometn.koemans.utils

import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSupertypeOf

enum class SimpleType(internal val kType: KType) {
    Boolean(kotlin.Boolean::class.createType()),
    Number(kotlin.Number::class.createType()),
    String(kotlin.String::class.createType()),
    Binary(ByteArray::class.createType()),
    Null(Nothing::class.createType()),
    Object(Any::class.createType()),
    Collection(kotlin.collections.Collection::class.createType(listOf(KTypeProjection.STAR)));

    companion object {
        internal val orderedNormalTypes = listOf(
            String,
            Boolean,
            Number,
            Binary,
            Collection
        )

        val Undefined = Null
    }
}

fun Any?.typeLiteral(): SimpleType {
    if(this == null) return SimpleType.Null
    val kType = this::class.createType()
    return SimpleType.orderedNormalTypes.firstOrNull { it.kType.isSupertypeOf(kType) } ?: SimpleType.Object
}

fun KType.toLiteralType(): SimpleType {
    return SimpleType.orderedNormalTypes.firstOrNull { it.kType.isSupertypeOf(this) } ?: SimpleType.Object
}