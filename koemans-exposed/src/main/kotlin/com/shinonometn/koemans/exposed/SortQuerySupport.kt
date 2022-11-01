package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import kotlin.reflect.KProperty

class SortOption(val column: Column<*>, val sortOrder: SortOrder)

class SortRequest(val defaultSortOrder: SortOrder, internal val mapping: Map<String, SortOption>) {
    val fields = mapping.map { it.key to it.value.sortOrder }

    fun isEmpty() = mapping.isEmpty()

    companion object {
        private val emptySortOption = SortRequest(SortOrder.ASC, emptyMap())

        fun empty() = emptySortOption
    }
}

fun Query.orderBy(sortRequest: SortRequest): Query {
    val options = sortRequest.mapping.map { it.value.column to it.value.sortOrder }.takeIf {
        it.isNotEmpty()
    } ?: return this

    return orderBy(*options.toTypedArray())
}

class SortOptionValidationException(message: String) : Exception(message)

class SortOptionMapping internal constructor(private val config: Configuration) {

    constructor(builder: Configuration.() -> Unit) : this(Configuration()) {
        config.builder()
    }

    companion object {
        private val allowedSortOptions = SortOrder.values().associateBy { it.name }
    }

    operator fun invoke(pairs: Collection<Pair<String, String?>>) = buildSortRequest(pairs)

    fun buildSortRequest(pairs: Collection<Pair<String, String?>>): SortRequest {
        val mapping = config.fields
        val options = pairs.toMap()

        val result = mapping.map { o ->
            val fieldName = o.key
            val sortMeta = o.value

            val sortOrder = options[fieldName]?.let {
                allowedSortOptions[it.uppercase()] ?: throw SortOptionValidationException("invalid_sort_option:$fieldName,$it")
            } ?: sortMeta.sortOrder

            fieldName to SortOption(sortMeta.column, sortOrder)
        }.takeIf { it.isNotEmpty() } ?: config.fields.map { it.key to it.value }

        return SortRequest(config.defaultSortOrder, result.toMap())
    }

    class Configuration {
        internal val fields = LinkedHashMap<String, SortOption>()
        var defaultSortOrder = SortOrder.ASC

        val ASC = SortOrder.ASC
        val DESC = SortOrder.DESC

        infix fun KProperty<*>.associateTo(column: Column<*>): Pair<String, SortOption> {
            val name = this.name
            val option = SortOption(column, defaultSortOrder)
            fields[name] = option
            return name to option
        }

        infix fun String.associateTo(column: Column<*>): Pair<String, SortOption> {
            val option = SortOption(column, defaultSortOrder)
            fields[this] = option
            return this to option
        }

        infix fun Pair<String, SortOption>.defaultOrder(order: SortOrder): Pair<String, SortOption> {
            val o = SortOption(second.column, order)
            fields[first] = o
            return first to o
        }
    }

}