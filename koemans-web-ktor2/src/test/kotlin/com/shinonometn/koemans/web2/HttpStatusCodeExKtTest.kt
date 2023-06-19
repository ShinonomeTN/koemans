package com.shinonometn.koemans.web2

import io.ktor.http.*
import org.junit.Assert.*
import org.junit.Test

class HttpStatusCodeExKtTest {
    @Test
    fun `Teapot is here`() {
        assertEquals(HttpStatusCode.IAmATeapot, HttpStatusCode.IAmATeapot)
    }
}