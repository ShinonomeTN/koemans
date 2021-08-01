package com.shinonometn.koemans.utils

import org.junit.Assert.*
import org.junit.Test

class StringUtilsKtTest {
    @Test
    fun `string is number` () {
        listOf("1","23", "456", "789", "0").forEach {
            assertTrue("'$it' should be a number.", it.isNumber())
        }
    }

    @Test
    fun `string is not number`() {
        listOf("", "a", "n,", "11a", "a111", "_0_").forEach {
            assertFalse("'$it' should not be a number.", it.isNumber())
        }
    }

    @Test
    fun `string representing of numbers are decimals`() {
        listOf("1234.67", "1234.0", "321.0", "321.00", "321.0000000000", "1234").forEach {
            assertTrue("'$it' is an decimal.", it.isDecimal())
        }
    }

    @Test
    fun `string representing of numbers with symbol are decimals`() {
        listOf("+456.789", "-987.654", "+123", "-345", "+0.98", "-0.31").forEach {
            assertTrue("'$it' is an decimal.", it.isDecimal())
        }
    }

    @Test
    fun `invalid representing of numbers are not decimals`() {
        listOf("1234.56aa", "1234aa.56", "1234aa.56.aa", "1234.", ".56", ".").forEach {
            assertFalse("'$it' isn't an decimal.", it.isDecimal())
        }
    }

    @Test
    fun `decimal part length limits`() {
        listOf("123.0000", "123.5555", "123.39999", "+12234.33333", "-0.3333").forEach {
            assertFalse("'$it' decimal part longer than 2.", it.isDecimal(2))
        }

        listOf("123.0", "123.5", "123.39", "+12234.33", "-0.3").forEach {
            assertTrue("'$it' decimal part shorter or equals 2.", it.isDecimal(2))
        }
    }

    @Test
    fun `allow padding zeros`() {
        listOf("+000000000000", "-00000000000", "+000.998", "-0000099999.33").forEach {
            assertTrue("'$it' shall be considered as decimal.",it.isDecimal(allowLeftPaddingZeros = true))
        }
    }

    @Test
    fun `not allow padding zeros`() {
        listOf("+000000000000", "-00000000000", "+000.998", "-0000099999.33").forEach {
            assertFalse("'$it' shall not be considered as decimal.",it.isDecimal(allowLeftPaddingZeros = false))
        }
    }
}