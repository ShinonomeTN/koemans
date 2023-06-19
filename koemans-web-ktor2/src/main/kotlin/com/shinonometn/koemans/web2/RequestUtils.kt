package com.shinonometn.koemans.web2

import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*

fun ApplicationRequest.xForwardedProtocol(): String? {
    return headers[HttpHeaders.XForwardedProto]
}

fun ApplicationRequest.clientIp(): String {
    return origin.remoteHost
}

fun ApplicationRequest.clientPort(): Int {
    return origin.remotePort
}

fun ApplicationRequest.uri(): String {
    return origin.uri
}