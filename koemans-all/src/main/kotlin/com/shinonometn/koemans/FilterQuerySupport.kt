package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.FilterOptionMapping
import com.shinonometn.koemans.exposed.FilterRequest
import io.ktor.application.*
import io.ktor.util.*

fun ApplicationCall.receiveFilterOptions(mapping : FilterOptionMapping) : FilterRequest {
    val params = request.queryParameters
    return mapping(params.toMap())
}
