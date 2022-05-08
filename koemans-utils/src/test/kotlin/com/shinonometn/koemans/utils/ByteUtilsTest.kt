package com.shinonometn.koemans.utils

import org.apache.commons.codec.binary.Hex
import org.junit.Test
import kotlin.test.assertTrue

class ByteUtilsTest {
    @Test
    fun `Test Long to bytes`() {
        val long = 0x1122334455667788
        val bytes = long.toByteArray()
        assertTrue(Hex.encodeHexString(bytes) == "1122334455667788")
    }

    @Test
    fun `Test Int to bytes`() {
        val int = 0x11223344
        val bytes = int.toByteArray()
        assertTrue(Hex.encodeHexString(bytes) == "11223344")
    }
}