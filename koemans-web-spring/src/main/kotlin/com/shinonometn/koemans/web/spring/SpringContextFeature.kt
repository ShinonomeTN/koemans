package com.shinonometn.koemans.web.spring

import com.shinonometn.koemans.spring.SpringContextConfiguration
import com.shinonometn.koemans.spring.annotationDrivenApplicationContext
import com.shinonometn.koemans.spring.registerSingletonBean
import io.ktor.application.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.support.GenericApplicationContext
import java.util.*
import java.util.function.Supplier
import kotlin.properties.Delegates

class SpringContext(configuration: Configuration) {
    val context: GenericApplicationContext = configuration.applicationContext
        ?: error("No application context configured.\n Use DSLs to configure context type.")

    internal val ktorApplication = configuration.ktorApplication!!

    internal val afterContextStart = configuration.afterContextStart
    internal val beforeContextClose = configuration.beforeContextClose

    class Configuration {
        internal var ktorApplication: Application by Delegates.notNull()

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

        fun Application.annotationDriven(autoConfigClazz: Class<*>, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            logger.info("Initializing Spring context.")

            val time = timing {
                ktorApplication = this

                applicationContext = annotationDrivenApplicationContext(autoConfigClazz) {
                    registerSingletonBean { ktorApplication }
                    configure?.invoke(this)
                }
            }

            logger.info("Spring context initialized in {}ms.", time)
        }

        fun Application.annotationDriven(vararg packages: String, configure: (SpringContextConfiguration.() -> Unit)? = null) {
            ktorApplication = this

            val time = timing {
                applicationContext = annotationDrivenApplicationContext(*packages) {
                    registerSingletonBean { ktorApplication }
                    configure?.invoke(this)
                }
            }

            logger.info("Spring context initialized in {}ms.", time)
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

val Application.springContext: ApplicationContext
    get() = feature(SpringContext).context