package com.shinonometn.koemans.exposed

import com.shinonometn.koemans.utils.UrlParameters
import com.shinonometn.koemans.utils.splitToPair
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import kotlin.reflect.KProperty

class SortOption(val column: Column<*>, val sortOrder: SortOrder)

/** A sort request */
class SortRequest(internal val mapping: Map<String, SortOption>) {
    val fields = mapping.map { it.key to it.value.sortOrder }

    fun isEmpty() = mapping.isEmpty()

    companion object {
        private val emptySortOption = SortRequest(emptyMap())

        fun empty() = emptySortOption
    }
}

/**
 * Build a sort option directly from url parameters using a SortOptionMapping
 *
 * @param parameters url parameters
 * @param mapping sort option mapping
 */
fun SortRequest.Companion.from(parameters: UrlParameters, mapping: SortOptionMapping): SortRequest {
    return mapping(parameters.filter { it.key == "sort" }.flatMap { it.value.mapNotNull { string -> string.splitToPair(',') } })
}

/**
 * Order by a sort request
 */
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

    private fun buildSortRequest(pairs: Collection<Pair<String, String?>>): SortRequest {
        val mapping = config.fields
        val parameters = pairs.toMap()

        val result = mapping.map { (fieldName, option) ->
            val sortOrder = when (val expected = parameters[fieldName]) {
                null -> option.sortOrder
                else -> allowedSortOptions[expected.uppercase()] ?: throw SortOptionValidationException("invalid_sort_option:$fieldName,$expected")
            }
            fieldName to SortOption(option.column, sortOrder)
        }.takeIf { it.isNotEmpty() } ?: config.fields.map { it.key to it.value }

        return SortRequest(result.toMap())
    }

    class Configuration {
        internal val fields = LinkedHashMap<String, SortOption>()

        /** set the default sort order */
        var defaultSortOrder = SortOrder.ASC

        /** represent ASC order */
        val ASC = SortOrder.ASC

        /** represent DESC order */
        val DESC = SortOrder.DESC

        /** Use a property as name for sort option */
        infix fun KProperty<*>.associateTo(column: Column<*>): Pair<String, SortOption> {
            val name = this.name
            val option = SortOption(column, defaultSortOrder)
            fields[name] = option
            return name to option
        }

        fun exclude(vararg property: KProperty<*>) {
            property.forEach { fields.remove(it.name) }
        }

        /** associate a parameter key to column */
        infix fun String.associateTo(column: Column<*>): Pair<String, SortOption> {
            val option = SortOption(column, defaultSortOrder)
            fields[this] = option
            return this to option
        }

        fun exclude(vararg fieldName: String) {
            fieldName.forEach { fields.remove(it) }
        }

        infix fun Pair<String, SortOption>.defaultOrder(order: SortOrder): Pair<String, SortOption> {
            val o = SortOption(second.column, order)
            fields[first] = o
            return first to o
        }

        internal fun copyFrom(another: Configuration): Configuration {
            fields.putAll(another.fields)
            defaultSortOrder = another.defaultSortOrder
            return this
        }
    }

    /** copy this mapping */
    fun copy(config: Configuration.() -> Unit): SortOptionMapping {
        return SortOptionMapping(Configuration().copyFrom(this.config).apply(config))
    }
}