package com.shinonometn.koemans.exposed

import org.jetbrains.exposed.sql.*

typealias PredicateFragmentBuilder = SqlExpressionBuilder.(FilterOptionMapping.ValueWrapper) -> Op<Boolean>

class FilterRequest internal constructor(internal var op : Op<Boolean>, val parameters : List<String>) {
    fun append(predicate : SqlExpressionBuilder.() -> Op<Boolean>) : Op<Boolean> {
        op = op.and(predicate)
        return op
    }
}

fun FieldSet.selectBy(filterRequest: FilterRequest, additionalBuilder : (SqlExpressionBuilder.(Op<Boolean>) -> Op<Boolean>)? = null) : Query {
    return additionalBuilder?.let { select { additionalBuilder(this, filterRequest.op)} } ?: select(filterRequest.op)
}

typealias FilterParams = Map<String, List<String>?>

class FilterOptionMapping internal constructor(val config : Configuration){

    constructor(builder : Configuration.() -> Unit) : this(Configuration()) {
        config.builder()
    }

    operator fun invoke(params : FilterParams) = buildFilterRequest(params)

    fun buildFilterRequest(params : FilterParams): FilterRequest {
        val keys = config.mapping.keys.filter {
            val param = params[it]
            param != null && param.isNotEmpty()
        }.toList()

        val fragments = keys.associateWith {
            config.mapping[it]!!.invoke(SqlExpressionBuilder, ValueWrapper(it, params))
        }

        val op = if(fragments.isNotEmpty()) Op.TRUE else // Fix problem with empty filter
            config.expression(SqlExpressionBuilder, fragments)

        return FilterRequest(op, keys)
    }

    class ValueWrapper(val name : String, private val parameters : FilterParams) {
        fun asString() = parameters[name]!!.first()
        fun asList() : List<String> = parameters[name]!!
    }

    class Configuration {
        internal val mapping = LinkedHashMap<String, PredicateFragmentBuilder>()

        var expression : SqlExpressionBuilder.(Map<String, Op<Boolean>>) -> Op<Boolean> = {
            AndOp(it.values.toList())
        }

        infix fun String.means(builder : PredicateFragmentBuilder) : Pair<String, PredicateFragmentBuilder> {
            mapping[this] = builder
            return this to builder
        }

        fun exclude(vararg name : String) {
            name.forEach { mapping.remove(it) }
        }
    }

    fun copy(builder : Configuration.() -> Unit) : FilterOptionMapping {
        val newConfig = Configuration()
        newConfig.mapping.putAll(config.mapping)
        newConfig.expression = config.expression
        newConfig.builder()
        return FilterOptionMapping(newConfig)
    }
}