package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.FilterOptionMapping
import com.shinonometn.koemans.exposed.FilterRequest
import io.ktor.application.*
import io.ktor.util.*

@Deprecated("Deprecated. Use builder instead", ReplaceWith("FilterRequest.from(call.request.queryParameters, mapping)"))
fun ApplicationCall.receiveFilterOptions(mapping : FilterOptionMapping) : FilterRequest {
    val params = request.queryParameters
    return mapping(params.toMap())
}
