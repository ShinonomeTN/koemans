package com.shinonometn.koemans

import com.shinonometn.koemans.exposed.SortOptionMapping
import com.shinonometn.koemans.exposed.SortRequest
import io.ktor.application.*

fun ApplicationCall.receiveSortOptions(mapping: SortOptionMapping): SortRequest {
    val params = request.queryParameters.getAll("sort")?.takeIf {
        it.isNotEmpty()
    } ?: emptyList()

    return mapping(params.mapNotNull { it.splitToPair() })
}

private fun String.splitToPair(): Pair<String, String?>? {
    val array = split(",").takeIf { it.isNotEmpty() } ?: return null
    return when (array.size) {
        1 -> Pair(array[0], null)
        else -> Pair(array[0], array[1])
    }
}