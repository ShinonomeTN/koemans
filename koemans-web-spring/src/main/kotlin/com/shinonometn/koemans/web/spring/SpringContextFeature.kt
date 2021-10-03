package com.shinonometn.koemans.web.spring

import io.ktor.application.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.*

private val logger = LoggerFactory.getLogger("SpringContext")

class SpringContext(configuration: Configuration) {
    val context: GenericApplicationContext = configuration.applicationContext
        ?: error("No application context configured.\n Use DSLs to configure context type.")

    internal val ktorApplication = configuration.ktorApplication!!

    internal val contextStartAction = configuration.whenApplicationStart
    internal val contextCloseAction = configuration.whenApplicationClose

    class Configuration {
        internal var ktorApplication: Application? = null

        internal var applicationContext: GenericApplicationContext? = null
            private set

        internal var whenApplicationStart: () -> Unit = {}
        internal var whenApplicationClose: () -> Unit = {}

        fun Application.annotationDriven(autoConfigClazz: Class<*>, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            logger.info("Initializing Spring context.")

            val time = timing {
                val c = AnnotationConfigApplicationContext()
                c.register(autoConfigClazz)

                ktorApplication = this
                applicationContext = c

                configure?.let {
                    val conf = SpringContextConfiguration().apply { it() }
                    conf.applyOn(c)
                }

                c.refresh()

                whenApplicationStart = { c.start() }
                whenApplicationClose = { c.close() }
            }

            logger.info("Spring context initialized in {}ms.", time)
        }

        fun Application.annotationDriven(vararg packages: String, configure: (SpringContextConfiguration.() -> Unit)? = null) {

            val c = AnnotationConfigApplicationContext()

            ktorApplication = this

            applicationContext = c

            val contextConfig = configure?.let { SpringContextConfiguration().apply { it() } }

            c.scan(*packages)
            contextConfig?.run { applyOn(c) }
            c.refresh()

            whenApplicationStart = { c.start() }
            whenApplicationClose = { c.close() }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SpringContext> {

        override val key: AttributeKey<SpringContext> = AttributeKey("SpringContext")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SpringContext {
            val configuration = Configuration().apply(configure)

            val feature = SpringContext(configuration)

            val application = feature.ktorApplication

            feature.contextStartAction()
            logger.info("Spring context started.")

            application.environment.monitor.let { e ->
                e.subscribe(ApplicationStopped) {
                    logger.info("Closing Spring context.")
                    feature.contextCloseAction()
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

class SpringContextConfiguration internal constructor() {
    private val additionalActions = LinkedList<GenericApplicationContext.() -> Unit>()

    fun additionalActions(action : GenericApplicationContext.() -> Unit) = additionalActions.add(action)

    internal fun applyOn(context: GenericApplicationContext) = additionalActions.forEach { context.it() }

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