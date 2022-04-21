package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.*
import java.util.function.Supplier

class SpringContext(configuration: Configuration) {
    val context: GenericApplicationContext = configuration.applicationContext
        ?: error("No application context configured.\n Use DSLs to configure context type.")

    internal val ktorApplication = configuration.ktorApplication!!

    internal val afterContextStart = configuration.afterContextStart
    internal val beforeContextClose = configuration.beforeContextClose

    class Configuration {
        internal var ktorApplication: Application? = null

        internal var applicationContext: GenericApplicationContext? = null
            private set

        internal var afterContextStart: (SpringContext) -> Unit = {}
        fun afterContextStart(b: (SpringContext) -> Unit) {
            afterContextStart = b
        }

        internal var beforeContextClose: (SpringContext) -> Unit = {}
        fun beforeContextClose(b: (SpringContext) -> Unit) {
            beforeContextClose = b
        }

        private fun annotationContext(configure: (SpringContextConfiguration.() -> Unit)?) = AnnotationConfigApplicationContext().apply {
            registerBean(Application::class.java, Supplier { ktorApplication })
            SpringContextConfiguration(configure).applyOn(this)
        }

        fun Application.annotationDriven(autoConfigClazz: Class<*>, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            logger.info("Initializing Spring context.")

            val time = timing {
                ktorApplication = this

                val c = annotationContext(configure)
                applicationContext = c
                c.register(autoConfigClazz)
                c.refresh()
            }

            logger.info("Spring context initialized in {}ms.", time)
        }

        fun Application.annotationDriven(vararg packages: String, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            ktorApplication = this

            val c = annotationContext(configure)
            applicationContext = c

            c.scan(*packages)
            c.refresh()
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SpringContext> {
        val logger = LoggerFactory.getLogger("SpringContext")

        override val key: AttributeKey<SpringContext> = AttributeKey("SpringContext")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SpringContext {
            val configuration = Configuration().apply(configure)

            val feature = SpringContext(configuration)

            val application = feature.ktorApplication

            feature.context.start()
            feature.afterContextStart(feature)
            logger.info("Spring context started.")

            application.environment.monitor.let { e ->
                e.subscribe(ApplicationStopped) {
                    logger.info("Closing Spring context.")
                    feature.beforeContextClose(feature)
                    feature.context.close()
                }
            }

            return feature
        }
    }
}

private fun timing(task: () -> Unit): Long {
    val start = System.currentTimeMillis()
    task()
    return System.currentTimeMillis() - start
}

class SpringContextConfiguration internal constructor(builder: (SpringContextConfiguration.() -> Unit)? = null) {
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

        SpringContext.logger.info("Applying {} additional context configuration actions.", actions.size)

        actions.forEach { context.it() }
    }

    inline fun <reified T> registerBean(name: String, bean: T): SpringContextConfiguration {
        additionalActions { registerBean(name, bean!!::class.java, { bean }) }
        return this
    }

    inline fun <reified T> registerBean(bean: T): SpringContextConfiguration {
        additionalActions { registerBean(T::class.java, { bean }) }
        return this
    }
}

val Application.springContext: ApplicationContext
    get() = feature(SpringContext).context

inline fun <reified T> ApplicationContext.find(): T = getBean(T::class.java)

inline fun <reified T> ApplicationContext.find(name: String): T = getBean(name, T::class.java)