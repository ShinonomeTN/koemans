package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.PageRequest
import com.shinonometn.koemans.utils.isNumber
import io.ktor.application.*
@Deprecated("Deprecated. Use builder instead", ReplaceWith("PageRequest.from(call.request.queryParameters)"))
fun ApplicationCall.receivePageRequest(defaultPage: Long = 0, defaultSize: Long = 20): PageRequest {
    val params = request.queryParameters
    val page = params["page"]?.takeIf { it.isNumber() }?.toLong() ?: defaultPage
    val size = params["size"]?.takeIf { it.isNumber() && it.toInt() > 0 }?.toLong() ?: defaultSize

    return PageRequest(page, size)
}
