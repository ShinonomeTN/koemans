package com.shinonometn.koemans.web.spring

import com.shinonometn.koemans.spring.SpringContextConfiguration
import com.shinonometn.koemans.spring.annotationDrivenApplicationContext
import io.ktor.application.*
import io.ktor.util.*
import io.noxpay.framework.spring.web.event.KtorApplicationStartedEvent
import io.noxpay.framework.spring.web.event.KtorApplicationStoppedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import kotlin.properties.Delegates

class SpringContext(configuration: Configuration) {
    val context: GenericApplicationContext = configuration.applicationContextFactory()
    val bindLifecycleToKtorApplication = configuration.bindLifecycleToKtorApplication

    internal val ktorApplication = configuration.ktorApplication

    internal val afterContextStart = configuration.afterContextStart
    internal val beforeContextClose = configuration.beforeContextClose

    class Configuration {
        internal var ktorApplication: Application by Delegates.notNull()

        internal var applicationContextFactory: () -> GenericApplicationContext = {
            error(
                "No application context configured.\n" +
                        " Use DSLs to configure context type. " +
                        "Or provide a application context factory using applicationContext()."
            )
        }

        internal val additionalApplicationContextConfigurations = mutableListOf<GenericApplicationContext.() -> Unit>({
            // register the ktor application to spring
            beanFactory.registerSingleton("ktorApplication", ktorApplication)
        })

        /** add singleton bean to spring context */
        fun registerSingleton(name: String, bean: Any) {
            additionalApplicationContextConfigurations.add { beanFactory.registerSingleton(name, bean) }
        }

        /** add singleton bean to spring context */
        fun registerSingleton(bean: Any) {
            registerSingleton(bean::class.java.canonicalName, bean)
        }

        /**
         * Is the spring context's lifecycle should bind to current ktor application or not
         *
         * If ktor is not the master of the application, you can set it to false
         */
        var bindLifecycleToKtorApplication = true

        internal var afterContextStart: (SpringContext) -> Unit = {}
        fun afterContextStart(b: (SpringContext) -> Unit) {
            afterContextStart = b
        }

        internal var beforeContextClose: (SpringContext) -> Unit = {}
        fun beforeContextClose(b: (SpringContext) -> Unit) {
            beforeContextClose = b
        }

        /** build spring application context */
        fun Application.applicationContext(provider: () -> GenericApplicationContext) {
            ktorApplication = this
            applicationContextFactory = {
                val start = System.currentTimeMillis()
                // build application context
                provider().apply {
                    // set context classloader same as application, this is important
                    classLoader = ktorApplication.environment.classLoader

                    additionalApplicationContextConfigurations.forEach { it() }
                }.also { logger.info("Spring context created in {}ms.", System.currentTimeMillis() - start) }
            }
        }

        fun Application.annotationDriven(autoConfigClazz: Class<*>, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            applicationContext { annotationDrivenApplicationContext(autoConfigClazz) { configure?.invoke(this) } }
        }

        fun Application.annotationDriven(vararg packages: String, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            applicationContext { annotationDrivenApplicationContext(*packages) { configure?.invoke(this) } }
        }
    }

    private fun startContext() {
        val start = System.currentTimeMillis()
        context.apply {
            refresh()
            start()
        }
        logger.info("Spring context refresh and started in {}ms.", System.currentTimeMillis() - start)
    }

    private fun closeContext() {
        logger.info("Closing Spring context.")
        context.close()
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, SpringContext> {
        val logger: Logger = LoggerFactory.getLogger(SpringContext::class.java)

        override val key: AttributeKey<SpringContext> = AttributeKey("SpringContext")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): SpringContext {
            val configuration = Configuration().apply(configure)
            val feature = SpringContext(configuration)
            val application = feature.ktorApplication
            val bindLifecycle = feature.bindLifecycleToKtorApplication

            application.environment.monitor.let { e ->
                val context = feature.context
                if (bindLifecycle) feature.startContext()

                e.subscribe(ApplicationStarting) {
                    context.publishEvent(KtorApplicationStartedEvent(application))
                    feature.afterContextStart(feature)
                }

                e.subscribe(ApplicationStopped) {
                    context.publishEvent(KtorApplicationStoppedEvent(application))
                    feature.beforeContextClose(feature)
                    if (bindLifecycle) feature.closeContext()
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

val Application.springContext: ApplicationContext
    get() = feature(SpringContext).context