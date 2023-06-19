package com.shinonometn.koemans.web.spring.route

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class KtorRoute(val route : String = "")
