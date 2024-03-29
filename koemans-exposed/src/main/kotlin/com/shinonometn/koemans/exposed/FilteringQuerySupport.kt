package com.shinonometn.koemans.exposed

import com.shinonometn.koemans.utils.UrlParameters
import org.jetbrains.exposed.sql.*

typealias PredicateFragmentBuilder = SqlExpressionBuilder.(FilterOptionMapping.ValueWrapper) -> Op<Boolean>

class FilterRequest internal constructor(internal var op: Op<Boolean>, val parameters: List<String>) {
    @Deprecated("Deprecated", ReplaceWith("appendAnd()"))
    fun append(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Op<Boolean> {
        return appendAnd(predicate)
    }

    /** Append predicate using AND op*/
    fun appendAnd(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Op<Boolean> {
        op = op.and(predicate)
        return op
    }

    /** Append predicate using OR op*/
    fun appendOr(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Op<Boolean> {
        op = op.or(predicate)
        return op
    }

    fun isEmpty() = (op == Op.TRUE)

    companion object
}

/** build filter request directly from url parameters */
fun FilterRequest.Companion.from(parameters: UrlParameters, mapping: FilterOptionMapping): FilterRequest {
    return mapping(parameters)
}

/**
 * Select by a filter request
 *
 * @param filterRequest filter request built from FilterOptionMapping
 * @param additionalBuilder chain the filter request's predicates with custom predicates
 *
 * @return Exposed query
 */
fun FieldSet.selectBy(filterRequest: FilterRequest, additionalBuilder: (SqlExpressionBuilder.(filtering : Op<Boolean>) -> Op<Boolean>)? = null): Query {

    // When no params in filter, use the additional builder.
    // If no provided builder, just return selectAll()
    if (filterRequest.isEmpty()) return when (additionalBuilder) {
        null -> selectAll()
        else -> select { additionalBuilder(this, Op.TRUE) }
    }

    // When there is no additional builder, use the filterRequest.op
    // If not, combine the filterRequest.op and the exp provided by additional builder
    return when(additionalBuilder) {
        null -> select { filterRequest.op }
        else -> select { additionalBuilder(this, filterRequest.op) }
    }
}

/**
 *
 * Filter option mapping
 *
 */
class FilterOptionMapping internal constructor(private val config: Configuration) {

    constructor(builder: Configuration.() -> Unit) : this(Configuration()) {
        config.builder()
    }

    operator fun invoke(params: UrlParameters) = buildFilterRequest(params)

    private fun buildFilterRequest(params: UrlParameters): FilterRequest {
        val validator = config.validator
        if(validator != null) validator(params)

        val keys = config.mapping.keys.filter { !params[it].isNullOrEmpty() }.toList()

        val fragments = keys.associateWith {
            config.mapping[it]!!.invoke(SqlExpressionBuilder, ValueWrapper(it, params))
        }

        val op = if (fragments.isEmpty()) Op.TRUE else // Fix problem with empty filter
            config.opBuilder(SqlExpressionBuilder, fragments)

        return FilterRequest(op, keys)
    }

    class ValueWrapper(val name: String, val parameters: UrlParameters) {
        /** take first value */
        fun asString() = parameters[name]!!.first()

        /** get all values */
        @Deprecated("Deprecated", ReplaceWith("asStringList()"), DeprecationLevel.WARNING)
        fun asList(): List<String> = asStringList()

        /** get all values */
        fun asStringList() = parameters[name]!!

        fun <T> convertTo(converter: (List<String>) -> T) = converter(parameters[name]!!)
    }

    class Configuration {
        internal val mapping = LinkedHashMap<String, PredicateFragmentBuilder>()

        /** Set the validator for incoming parameters */
        var validator : ((UrlParameters) -> Unit)? = null

        /** overwrite the default sql op builder */
        var opBuilder: SqlExpressionBuilder.(predicateMap : Map<String, Op<Boolean>>) -> Op<Boolean> = {
            AndOp(it.values.toList())
        }

        infix fun String.means(builder: PredicateFragmentBuilder): Pair<String, PredicateFragmentBuilder> {
            mapping[this] = builder
            return this to builder
        }

        fun exclude(vararg name: String) {
            name.forEach { mapping.remove(it) }
        }

        internal fun copyFrom(another: Configuration): Configuration {
            mapping.putAll(another.mapping)
            opBuilder = another.opBuilder
            validator = another.validator
            return this
        }
    }

    fun copy(builder: Configuration.() -> Unit): FilterOptionMapping {
        return FilterOptionMapping(Configuration().copyFrom(config).apply(builder))
    }
}