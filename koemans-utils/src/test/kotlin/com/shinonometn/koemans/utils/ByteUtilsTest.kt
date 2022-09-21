package com.shinonometn.koemans.utils

import org.apache.commons.codec.binary.Hex
import org.junit.Test
import kotlin.random.Random
import kotlin.test.assertEquals

class ByteUtilsTest {
    @Test
    fun `Test Long to bytes`() {
        val long = 0x1122334455667788
        val bytes = long.toByteArray()
        assertEquals("1122334455667788", Hex.encodeHexString(bytes))
    }

    @Test
    fun `Test Bytes to Long oversize`() {
        val long = byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, 0x88.toByte()).toLong()
        assertEquals(0x0011223344556677, long)
    }

    @Test
    fun `Test Bytes to Long full length`() {
        val long = byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77).toLong()
        assertEquals(0x0011223344556677L, long)
    }

    @Test
    fun `Test Bytes to Long Half length`() {
        val long = byteArrayOf(0x11, 0x22, 0x33, 0x44, 0x55).toLong()
        assertEquals(0x1122334455L, long)
    }

    @Test
    fun `Test Bytes to Long empty`() {
        val long = byteArrayOf().toLong()
        assertEquals(0L, long)
    }

    @Test
    fun `Test Bytes to Long with offset`() {
        repeat(1000) {
            val expected = Random.nextLong(Long.MAX_VALUE)
            val bs = expected.toByteArray()
            listOf(
                byteArrayOf(0, 0, *bs, 0, 0) to (2..9),
                byteArrayOf(*bs, 0, 0) to (0..7),
                byteArrayOf(0, 0, 0, *bs) to (3..10)
            ).forEach { (arr, range) ->
                val actual = arr.toLong(range)
                assertEquals(expected, actual, "Should equals")
            }
        }
    }

    /* Integer */

    @Test
    fun `Test Int to bytes`() {
        val int = 0x11223344
        val bytes = int.toByteArray()
        assertEquals("11223344", Hex.encodeHexString(bytes))
    }

    @Test
    fun `Test Bytes to Int oversize`() {
        val int = byteArrayOf(0x00, 0x11, 0x22, 0x33, 0x44).toInt()
        assertEquals(0x00112233, int)
    }

    @Test
    fun `Test Bytes to Int full length`() {
        val int = byteArrayOf(0x00, 0x11, 0x22, 0x33).toInt()
        assertEquals(0x00112233, int)
    }

    @Test
    fun `Test Bytes to Int half length`() {
        val int = byteArrayOf(0x00, 0x11).toInt()
        assertEquals(0x0011, int)
    }

    @Test
    fun `Test Bytes to Int empty`() {
        val int = byteArrayOf().toInt()
        assertEquals(0, int)
    }

    @Test
    fun `Test Bytes to Int with offset`() {
        repeat(1000) {
            val expected = Random.nextInt(Int.MAX_VALUE)
            val byteArray = byteArrayOf(0, 0, *(expected.toByteArray()))
            val actual = byteArray.toInt(2)
            assertEquals(expected, actual, "Should equals")
        }
    }

    @Test
    fun `Test Bytes to Int with range`() {
        repeat(1000) {
            val expected = Random.nextInt(Int.MAX_VALUE)
            val bs = expected.toByteArray()
            listOf(
                byteArrayOf(0, 0, *bs, 0, 0) to (2..5),
                byteArrayOf(*bs, 0, 0) to (0..3),
                byteArrayOf(0, 0, 0, *bs) to (3..6)
            ).forEach { (arr, range) ->
                val actual = arr.toInt(range)
                assertEquals(expected, actual, "Should equals")
            }
        }
    }
}