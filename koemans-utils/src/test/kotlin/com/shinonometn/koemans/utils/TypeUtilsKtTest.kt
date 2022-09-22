package com.shinonometn.koemans.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.math.BigInteger
import kotlin.reflect.full.createType

class TypeUtilsKtTest {
    @Test
    fun `Test get literal type`() {
        listOf(
            SimpleType.Boolean to true,
            SimpleType.Number to 1.toByte(),
            SimpleType.Number to 1.toShort(),
            SimpleType.Number to 10,
            SimpleType.Number to 1000L,
            SimpleType.Number to BigInteger.ONE,
            SimpleType.Number to 1.0,
            SimpleType.Number to 1.0f,
            SimpleType.Number to BigDecimal.ONE,
            SimpleType.Binary to byteArrayOf(),
            SimpleType.Object to Any(),
            SimpleType.Collection to listOf<String>(),
            SimpleType.Null to null
        ).forEach { assertEquals(it.first, it.second.typeLiteral()) }
    }

    @Test
    fun `Test get by kType`() {
        listOf(
            SimpleType.Boolean to true::class.createType(),
            SimpleType.Number to 1.toByte()::class.createType(),
            SimpleType.Number to 1.toShort()::class.createType(),
            SimpleType.Number to 1::class.createType(),
            SimpleType.Number to 1.toLong()::class.createType(),
            SimpleType.Number to 1.toBigInteger()::class.createType(),
            SimpleType.Number to 1.toFloat()::class.createType(),
            SimpleType.Number to 1.toDouble()::class.createType(),
            SimpleType.Number to 1.toBigDecimal()::class.createType(),
            SimpleType.Binary to byteArrayOf()::class.createType(),
            SimpleType.Collection to listOf<Any>()::class.createType(),
            SimpleType.Object to Any()::class.createType(),
        ).forEach { assertEquals(it.first, it.second.toLiteralType()) }
    }
}