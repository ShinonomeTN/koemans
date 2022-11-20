package com.shinonometn.koemans.exposed

import com.shinonometn.koemans.utils.UrlParameters
import org.jetbrains.exposed.sql.*
import kotlin.reflect.KClass

typealias PredicateFragmentBuilder = SqlExpressionBuilder.(FilterOptionMapping.ValueWrapper) -> Op<Boolean>

class FilterRequest internal constructor(internal var op: Op<Boolean>, val parameters: List<String>) {
    fun append(predicate: SqlExpressionBuilder.() -> Op<Boolean>): Op<Boolean> {
        op = op.and(predicate)
        return op
    }

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
    if (filterRequest.parameters.isEmpty()) return when (additionalBuilder) {
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
        fun asString() = parameters[name]!!.first()
        fun asList(): List<String> = parameters[name]!!
        fun <T> convertTo(converter : (List<String>) -> T) = converter(parameters[name]!!)
    }

    class Configuration {
        internal val mapping = LinkedHashMap<String, PredicateFragmentBuilder>()

        /** Set the validator for incoming parameters */
        var validator : ((UrlParameters) -> Unit)? = null

        /** overwrite the default op builder */
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
    }

    fun copy(builder: Configuration.() -> Unit): FilterOptionMapping {
        val newConfig = Configuration()
        newConfig.mapping.putAll(config.mapping)
        newConfig.opBuilder = config.opBuilder
        newConfig.builder()
        return FilterOptionMapping(newConfig)
    }
}

fun FilterOptionMapping.ValueWrapper.asLong() = convertTo { it.first().toLong() }

fun FilterOptionMapping.ValueWrapper.asLongList() = convertTo { it.map { s -> s.toLong() } }

fun FilterOptionMapping.ValueWrapper.asInt() = convertTo { it.first().toInt() }

fun FilterOptionMapping.ValueWrapper.asIntList() = convertTo { it.map { s -> s.toInt() } }

fun FilterOptionMapping.ValueWrapper.asBoolean() = convertTo { it.first() != "false" }

fun FilterOptionMapping.ValueWrapper.asBooleanList() = convertTo { it.map { s -> s != "false" } }

fun FilterOptionMapping.ValueWrapper.asDouble() = convertTo { it.first().toDouble() }

fun FilterOptionMapping.ValueWrapper.asDoubleList() = convertTo { it.map { s -> s.toDouble() } }

fun FilterOptionMapping.ValueWrapper.asFloat() = convertTo { it.first().toFloat() }

fun FilterOptionMapping.ValueWrapper.asFloatList() = convertTo { it.map { s -> s.toFloat() } }

fun FilterOptionMapping.ValueWrapper.asBigDecimal() = convertTo { it.first().toBigDecimal() }

fun FilterOptionMapping.ValueWrapper.asBigDecimalList() = convertTo { it.map { s -> s.toBigDecimal() } }

fun <T : Enum<T>> FilterOptionMapping.ValueWrapper.asEnum(klazz : KClass<T>): T = convertTo {
    klazz.java.enumConstants.first { e -> it.first() == e.name }
}

fun <T : Enum<T>> FilterOptionMapping.ValueWrapper.asEnumList(klazz: KClass<T>) : List<T> = convertTo {
    it.map { s -> klazz.java.enumConstants.first { e -> e.name == s } }
}