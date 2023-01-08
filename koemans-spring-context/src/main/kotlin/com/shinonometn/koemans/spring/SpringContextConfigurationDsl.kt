package com.shinonometn.koemans.spring

import org.springframework.context.ApplicationContext

/** Register a singleton bean to context */
inline fun <reified T : Any> SpringContextConfiguration.registerSingletonBean(name: String, bean: T): SpringContextConfiguration {
    additionalActions { beanFactory.registerSingleton(name, bean) }
    return this
}

/** Register a singleton bean to context using a provider */
inline fun <reified T : Any> SpringContextConfiguration.registerSingletonBean(crossinline supplier: ApplicationContext.() -> T) : SpringContextConfiguration {
    additionalActions { beanFactory.registerSingleton(T::class.java.canonicalName, supplier()) }
    return this
}