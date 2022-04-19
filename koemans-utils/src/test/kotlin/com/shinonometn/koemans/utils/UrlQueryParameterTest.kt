package com.shinonometn.koemans.utils

import org.junit.Test

import org.junit.Assert.*

class UrlQueryParameterTest {

    @Test
    fun toUrlEncoded() {
        val params = UrlQueryParameter {
            append("a", "b")
            append("c", "d")
            append("e", "f")
            append("e", "g")
            append("e", "\\")
        }

        assertEquals("a=b&c=d&e=f&e=g&e=%5C", params.toUrlEncoded())

        assertEquals("a=b&c=d&e=f,g,%5C", params.toUrlEncoded(false))
    }

    @Test
    fun `fromString 1`() {
        val params = UrlQueryParameter.fromString("a=b&c=d&e=f&e=g&e=%5C")
        val e = params["e"]
        assertNotNull(e)
        assertEquals(e!!.size, 3)
        val set = setOf("f", "g", "\\")
        assertTrue(e.all { set.contains(it) })
        assertEquals(params["a"]!!.first(), "b")
        assertEquals(params["c"]!!.first(), "d")
    }

    @Test
    fun `fromString 2`() {
        val params = UrlQueryParameter.fromString("a=b&c=d&e=f,g,%5C")
        val set = setOf("f", "g", "\\")
        assertTrue(params["e"]!!.all { set.contains(it) })
    }
}