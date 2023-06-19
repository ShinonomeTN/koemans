package com.shinonometn.koemans.web.spring.configuration

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
annotation class KtorConfiguration(val priority : Double = 1.0)
