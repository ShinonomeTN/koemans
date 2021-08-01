package com.shinonometn.koemans.web

import io.ktor.http.*

val HttpStatusCode.Companion.IAmATeapot: HttpStatusCode
    get() = HttpStatusCode(418, "I'm a teapot")