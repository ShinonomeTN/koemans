package com.shinonometn.koemans.spring.condition

import org.springframework.context.annotation.Conditional
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnBeanCondition::class)
annotation class WithConditionOnBean(val classes: Array<KClass<*>> = [], val names : Array<String> = [])