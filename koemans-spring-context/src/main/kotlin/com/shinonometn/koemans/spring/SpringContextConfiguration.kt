package com.shinonometn.koemans.spring

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.*
import java.util.function.Supplier

class SpringContextConfiguration internal constructor(builder: (SpringContextConfiguration.() -> Unit)? = null) {
    private val logger = LoggerFactory.getLogger("SpringContext")

    private val additionalActions = LinkedList<GenericApplicationContext.() -> Unit>()

    init {
        builder?.invoke(this)
    }

    /**
     * Additional Actions to Spring Context
     * It will execute before the context start to scan and load.
     */
    fun additionalActions(action: GenericApplicationContext.() -> Unit) = additionalActions.add(action)

    internal fun applyOn(context: GenericApplicationContext) {
        val actions = additionalActions.toList()
        logger.info("Applying {} additional context configuration actions.", actions.size)
        actions.forEach { context.it() }
    }


    @Deprecated("use `registerSingletonBean()` instead")
    inline fun <reified T> registerBean(name: String, bean: T): SpringContextConfiguration {
        additionalActions { registerBean(name, bean!!::class.java, { bean }) }
        return this
    }

    @Deprecated("use `registerSingletonBean()` instead")
    inline fun <reified T> registerBean(supplier: Supplier<T?>) : SpringContextConfiguration {
        additionalActions { registerBean(T::class.java, supplier) }
        return this
    }

    @Deprecated("use `registerSingletonBean()` instead")
    inline fun <reified T> registerBean(bean: T): SpringContextConfiguration {
        additionalActions { registerBean(T::class.java, { bean }) }
        return this
    }
}