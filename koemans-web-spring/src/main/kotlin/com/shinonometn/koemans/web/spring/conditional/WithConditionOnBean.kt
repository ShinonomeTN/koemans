package com.shinonometn.koemans.web.spring.conditional

import org.springframework.context.annotation.Conditional
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(OnBeanCondition::class)
annotation class WithConditionOnBean(val classes: Array<KClass<*>> = [], val names : Array<String> = [])
