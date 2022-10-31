package com.shinonometn.koemans.spring

import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext
import org.springframework.context.support.StaticApplicationContext

/**
 * Create an empty AnnotationConfigApplicationContext.
 *
 * **You should invoke `refresh()` and `start()` before using it.**
 */
fun annotationDrivenApplicationContext(configure: (SpringContextConfiguration.() -> Unit)? = null): GenericApplicationContext {
    val applicationContext = AnnotationConfigApplicationContext()
    SpringContextConfiguration(configure).applyOn(applicationContext)
    return applicationContext
}

/**
 * Create an AnnotationConfigApplicationContext, use [autoConfigurationClasses].
 *
 * Returns an application context that waiting to be started.
 *
 * **`refresh()` was automatically be invoked. You should call `start()` before using it.**
 */
fun annotationDrivenApplicationContext(
    vararg autoConfigurationClasses: Class<*>,
    configure: (SpringContextConfiguration.() -> Unit)? = null
): GenericApplicationContext {
    val applicationContext = AnnotationConfigApplicationContext()
    SpringContextConfiguration(configure).applyOn(applicationContext)
    applicationContext.register(*autoConfigurationClasses)
    applicationContext.refresh()
    return applicationContext
}

/**
 * Create an AnnotationConfigApplicationContext, scanning given [scanPackageNames].
 *
 * Returns an application context that waiting to be started
 * *You should invoke `start()` before using it.*
 */
fun annotationDrivenApplicationContext(
    vararg scanPackageNames: String,
    configure: (SpringContextConfiguration.() -> Unit)? = null
): GenericApplicationContext {
    val applicationContext = AnnotationConfigApplicationContext()
    SpringContextConfiguration(configure).applyOn(applicationContext)
    applicationContext.scan(*scanPackageNames)
    applicationContext.refresh()
    return applicationContext
}

/** Create an empty spring context */
fun staticApplicationContext(
    configure : (SpringContextConfiguration.() -> Unit)? = null
) : GenericApplicationContext {
    val applicationContext = StaticApplicationContext()
    SpringContextConfiguration(configure).applyOn(applicationContext)
    applicationContext.refresh()
    applicationContext.start()
    return applicationContext
}

inline fun <reified T> ApplicationContext.find(): T = getBean(T::class.java)

inline fun <reified T> ApplicationContext.find(name: String): T = getBean(name, T::class.java)