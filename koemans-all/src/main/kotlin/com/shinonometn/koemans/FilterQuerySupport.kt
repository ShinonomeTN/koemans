package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.FilterOptionMapping
import com.shinonometn.koemans.exposed.FilterRequest
import com.shinonometn.koemans.exposed.from
import io.ktor.application.*
import io.ktor.util.*

@Deprecated(
    "Deprecated. Use builder instead",
    ReplaceWith(
        "FilterRequest.from(call.request.queryParameters.toMap(), mapping)",
        "com.shinonometn.koemans.exposed.FilterRequest"
    )
)
fun ApplicationCall.receiveFilterOptions(mapping: FilterOptionMapping) = FilterRequest.from(request.queryParameters.toMap(), mapping)
