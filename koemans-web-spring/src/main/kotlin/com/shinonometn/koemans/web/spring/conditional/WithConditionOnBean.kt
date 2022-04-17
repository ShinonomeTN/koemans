package com.shinonometn.koemans.web.spring.conditional

import org.springframework.context.annotation.Conditional
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnBeanCondition::class)
annotation class WithConditionOnBean(vararg val value: KClass<*>)
