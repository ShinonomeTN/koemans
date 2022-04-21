package com.shinonometn.koemans.spring.condition

import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(SpringELExpressionCondition::class)
annotation class WithConditionExpression(@Language("SpEL") val value: String)