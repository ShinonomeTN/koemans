package com.shinonometn.koemans.web2

import io.ktor.http.*

private val HttpStatusCodeIAmATeapot = HttpStatusCode(418, "I'm a Teapot")

val HttpStatusCode.Companion.IAmATeapot : HttpStatusCode
    get() = HttpStatusCodeIAmATeapot