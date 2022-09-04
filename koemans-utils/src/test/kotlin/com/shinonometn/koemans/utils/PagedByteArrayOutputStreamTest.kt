package com.shinonometn.koemans.utils

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import kotlin.random.Random
import kotlin.random.nextInt

class PagedByteArrayOutputStreamTest {
    @Test
    fun `Test write, buffer equals to content size`() {
        val buffer = ByteArray(16)
        Random.nextBytes(buffer)
        val stream = PagedByteArrayOutputStream(pageSize = 16)
        stream.write(buffer)
        assertArrayEquals("ByteArray not equals!", buffer, stream.toByteArray())
    }

    @Test
    fun `Test write, buffer bigger than content size`() {
        val buffer = ByteArray(16)
        Random.nextBytes(buffer)
        val stream = PagedByteArrayOutputStream(pageSize = 32)
        stream.write(buffer)
        assertArrayEquals("ByteArray not equals!", buffer, stream.toByteArray())
    }

    @Test
    fun `Test write, buffer smaller than content size`() {
        val expected = Random.nextBytes(ByteArray(714))
        val stream = PagedByteArrayOutputStream(pageSize = 32)
        stream.write(expected)
        val actual = stream.toByteArray()
        assertArrayEquals("ByteArray not equals!", expected, actual)
    }

    @Test
    fun `Test write repeat`() {
        repeat(100000) {
            val expected = ByteArray(Random.nextInt(4096))
            Random.nextBytes(expected)
            val stream = PagedByteArrayOutputStream(pageSize = Random.nextInt(1 .. 1024))
            try {
                stream.write(expected)
            } catch (e : Exception) {
                throw Exception("Exception raise when calling write. data size ${expected.size}, quota ${stream.quota}, page size ${stream.pageSize}", e)
            }
            val actual = stream.toByteArray()
            assertArrayEquals("ByteArray not equals!", expected, actual)
        }
    }

    @Test
    fun `Test write to repeat`() {
        repeat(100000) {
            val expected = Random.nextBytes(ByteArray(Random.nextInt(4096)))
            val stream = PagedByteArrayOutputStream(pageSize = Random.nextInt(1 .. 1024))
            try {
                stream.write(expected)
            } catch (e : Exception) {
                throw Exception("Exception raise when calling write. data size ${expected.size}, quota ${stream.quota}, page size ${stream.pageSize}", e)
            }
            val output = ByteArrayOutputStream(expected.size)
            stream.writeTo(output)
            val actual = output.toByteArray()
            assertArrayEquals("ByteArray not equals!", expected, actual)
        }
    }

    @Test(expected = PagedByteArrayOutputStream.QuotaExcessException::class)
    fun `Test quota excess 1`() {
        val actual = Random.nextBytes(ByteArray(Random.nextInt(1024)))
        val stream = PagedByteArrayOutputStream(quota = actual.size / 2)
        stream.write(actual)
    }

    @Test(expected = PagedByteArrayOutputStream.QuotaExcessException::class)
    fun `Test quota excess 2`() {
        val actual = Random.nextBytes(ByteArray(Random.nextInt(1024)))
        val stream = PagedByteArrayOutputStream(quota = actual.size)
        stream.write(actual)
        stream.write(actual)
    }

    @Test(expected = PagedByteArrayOutputStream.QuotaExcessException::class)
    fun `Test quota excess 3`() {
        val actual = Random.nextBytes(ByteArray(Random.nextInt(1024)))
        val stream = PagedByteArrayOutputStream(quota = actual.size + (actual.size / 2))
        stream.write(actual)
        stream.write(actual)
    }

    @Test(expected = PagedByteArrayOutputStream.QuotaExcessException::class)
    fun `Test quota excess 4`() {
        val actual = Random.nextBytes(ByteArray(Random.nextInt(1024)))
        val stream = PagedByteArrayOutputStream(quota = actual.size)
        stream.write(actual)
        stream.write(0x00)
    }
}