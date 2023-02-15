package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.SortOptionMapping
import com.shinonometn.koemans.exposed.SortRequest
import com.shinonometn.koemans.exposed.from
import io.ktor.application.*
import io.ktor.util.*

@Deprecated(
    "Deprecated. Use builder instead",
    ReplaceWith(
        "SortRequest.from(call.request.queryParameters, mapping)",
        "com.shinonometn.koemans.exposed.SortRequest"
    )
)
fun ApplicationCall.receiveSortOptions(mapping: SortOptionMapping) = SortRequest.from(request.queryParameters.toMap(), mapping)