package com.shinonometn.koemans.web.spring.conditional

import org.intellij.lang.annotations.Language
import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(ExpressionConfigurationCondition::class)
annotation class WithConditionExpression(@Language("SpEL") val value: String)
