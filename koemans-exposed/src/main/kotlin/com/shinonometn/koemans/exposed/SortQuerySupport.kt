package com.shinonometn.koemans.exposed

import com.shinonometn.koemans.utils.UrlParameters
import com.shinonometn.koemans.utils.splitToPair
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder
import kotlin.reflect.KProperty

class SortOption(val column: Column<*>, val sortOrder: SortOrder)

/** A sort request */
class SortRequest(internal val mapping: Collection<Pair<String, SortOption>>) {
    val fields = mapping

    fun isEmpty() = mapping.isEmpty()

    companion object {
        private val emptySortOption = SortRequest(emptyList())

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
    if (sortRequest.isEmpty()) return this
    val options = sortRequest.mapping.map { (_, option) -> option.column to option.sortOrder }
    return orderBy(*options.toTypedArray())
}

class SortOptionMapping private constructor(private val config: Configuration) {

    constructor(builder: Configuration.() -> Unit) : this(Configuration()) {
        config.builder()
        config.updateImpliedFields()
    }

    companion object {
        private val allowedSortOptions = SortOrder.values().flatMap {
            listOf(it.name to it, it.name.lowercase() to it)
        }.toMap()
    }

    operator fun invoke(pairs: Collection<Pair<String, String?>> = emptyList()) = buildSortRequest(pairs)

    val fields = config.fields.values

    private fun buildSortRequest(pairs: Collection<Pair<String, String?>>): SortRequest {
        val mapping = config.fields
        val reqParams = pairs.distinctBy { it.first }
        val result = mutableListOf<Pair<String, SortOption>>()

        if(pairs.isNotEmpty()) for ((key, orderInStr) in reqParams) {
            val option = mapping[key] ?: continue

            val sortOrder = when {
                orderInStr.isNullOrEmpty() -> option.toSortOption()
                else -> option.toSortOption(allowedSortOptions[orderInStr])
            }

            result.add(key to sortOrder)
        }

        if(config.impliedFields.isNotEmpty()) {
            val parameterKeySet = reqParams.map(Pair<String, String?>::first).toSet()
            val impliedFields = config.impliedFields.entries.filter { (key, _) -> key !in parameterKeySet }
            if (impliedFields.isNotEmpty()) {
                for ((key, option) in impliedFields) result.add(key to option.toSortOption())
            }
        }

        return SortRequest(result)
    }

    class FieldOption internal constructor(
        val fieldKey: String,
        val column: Column<*>,
        val sortOrder: SortOrder,
        val implied: Boolean = false
    ) {
        internal fun withImplied(boolean: Boolean): FieldOption {
            return FieldOption(fieldKey, column, sortOrder, boolean)
        }

        fun toSortOption(order: SortOrder? = null) = SortOption(column, order ?: sortOrder)
    }

    class Configuration internal constructor() {
        internal val fields = LinkedHashMap<String, FieldOption>()

        internal val impliedFields = LinkedHashMap<String, FieldOption>()
        internal fun updateImpliedFields() : Configuration {
            impliedFields.clear()
            fields.entries.forEach { (key, option) ->
                if (option.implied) impliedFields[key] = option
            }
            return this
        }

        /** set the default sort order */
        var defaultSortOrder = SortOrder.ASC

        /** represent ASC order */
        val ASC = SortOrder.ASC

        /** represent DESC order */
        val DESC = SortOrder.DESC

        /** Use a property as name for sort option */
        infix fun KProperty<*>.associateTo(column: Column<*>): Pair<String, FieldOption> {
            val name = this.name
            val option = FieldOption(name, column, defaultSortOrder, false)
            fields[name] = option
            return name to option
        }

        fun exclude(vararg property: KProperty<*>) {
            property.forEach { fields.remove(it.name) }
        }

        /** associate a parameter key to column */
        infix fun String.associateTo(column: Column<*>): Pair<String, FieldOption> {
            val option = FieldOption(this, column, defaultSortOrder)
            fields[this] = option
            return this to option
        }

        fun exclude(vararg fieldName: String) {
            fieldName.forEach { fields.remove(it) }
        }

        infix fun Pair<String, FieldOption>.defaultOrder(order: SortOrder): Pair<String, FieldOption> {
            val o = FieldOption(first, second.column, order, false)
            fields[first] = o
            return first to o
        }

        fun FieldOption.implied(boolean: Boolean = true): FieldOption {
            val altered = withImplied(boolean)
            fields[fieldKey] = altered
            return altered
        }

        fun Pair<String, FieldOption>.implied(boolean: Boolean = true): FieldOption {
            val altered = FieldOption(first, second.column, second.sortOrder, boolean)
            fields[first] = altered
            return altered
        }

        fun Collection<String>.implied(boolean: Boolean = true) {
            for (field in this) {
                val old = fields[field] ?: continue
                fields[field] = old.withImplied(boolean)
            }
        }

        /**
         * Change implied fields.
         *
         * Note: This method does not change the origin field declaring order
         */
        fun impliedFields(vararg fieldKey: String) {
            for (field in fieldKey) {
                val old = fields[field] ?: continue
                fields[field] = old.withImplied(true)
            }
        }

        internal fun copyFrom(another: Configuration): Configuration {
            fields.putAll(another.fields)
            impliedFields.putAll(another.impliedFields)
            defaultSortOrder = another.defaultSortOrder
            return this
        }
    }

    /** copy this mapping */
    fun copy(config: Configuration.() -> Unit): SortOptionMapping {
        return SortOptionMapping(Configuration().copyFrom(this.config).apply(config).updateImpliedFields())
    }
}