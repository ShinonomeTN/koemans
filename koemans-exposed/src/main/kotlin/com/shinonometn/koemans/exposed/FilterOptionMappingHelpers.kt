package com.shinonometn.koemans.exposed

import kotlin.reflect.KClass

/*
*
*
* Value Wrapper helpers
*
*
*/

/** take first value and convert to long */
fun FilterOptionMapping.ValueWrapper.asLong() = convertTo { it.first().toLong() }

/** map all value to long */
fun FilterOptionMapping.ValueWrapper.asLongList() = convertTo { it.map { s -> s.toLong() } }

/** take first value and convert to int */
fun FilterOptionMapping.ValueWrapper.asInt() = convertTo { it.first().toInt() }

/** map all value to int */
fun FilterOptionMapping.ValueWrapper.asIntList() = convertTo { it.map { s -> s.toInt() } }

/** take first value and convert to boolean */
fun FilterOptionMapping.ValueWrapper.asBoolean() = convertTo { it.first() != "false" }

/** map all value to boolean */
fun FilterOptionMapping.ValueWrapper.asBooleanList() = convertTo { it.map { s -> s != "false" } }

/** take first value and convert to double */
fun FilterOptionMapping.ValueWrapper.asDouble() = convertTo { it.first().toDouble() }

/** map all value to double */
fun FilterOptionMapping.ValueWrapper.asDoubleList() = convertTo { it.map { s -> s.toDouble() } }

/** take first value and convert to float */
fun FilterOptionMapping.ValueWrapper.asFloat() = convertTo { it.first().toFloat() }

/** map all value to float */
fun FilterOptionMapping.ValueWrapper.asFloatList() = convertTo { it.map { s -> s.toFloat() } }

/** take first value and convert to BigDecimal */
fun FilterOptionMapping.ValueWrapper.asBigDecimal() = convertTo { it.first().toBigDecimal() }

/** map all value to BigDecimal */
fun FilterOptionMapping.ValueWrapper.asBigDecimalList() = convertTo { it.map { s -> s.toBigDecimal() } }

/** take first value and convert to enum */
fun <T : Enum<T>> FilterOptionMapping.ValueWrapper.asEnum(klazz: KClass<T>): T = convertTo {
    klazz.java.enumConstants.first { e -> it.first() == e.name }
}

/** map all value to enum */
fun <T : Enum<T>> FilterOptionMapping.ValueWrapper.asEnumList(klazz: KClass<T>): List<T> = convertTo {
    it.map { s -> klazz.java.enumConstants.first { e -> e.name == s } }
}