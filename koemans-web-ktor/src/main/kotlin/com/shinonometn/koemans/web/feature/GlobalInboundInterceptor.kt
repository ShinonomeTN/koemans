package com.shinonometn.koemans.web.feature

import com.shinonometn.koemans.web.KtorCallContext
import io.ktor.application.*
import io.ktor.util.*
import io.ktor.util.pipeline.*

private typealias CallInterceptor = suspend GlobalInboundInterceptor.InterceptContext.(KtorCallContext) -> Unit

/**
 * Global inbound interceptor.
 * Providing simple inbound check mechanism globally.
 */
class GlobalInboundInterceptor(configuration: Configuration) {
    private val interceptors: List<CallInterceptor> = configuration.inboundCheckers

    class InterceptContext {
        internal var finished = false
        internal var terminate = false

        /**
         * Finish intercepting for current call
         */
        fun finish() {
            finished = true
        }

        /**
         * Finish intercepting and terminate the call
         */
        fun terminate() {
            terminate = true
        }
    }

    class Configuration {
        internal var inboundCheckers = mutableListOf<CallInterceptor>()
        fun addInterceptor(block: CallInterceptor) = inboundCheckers.add(block)
    }

    companion object Feature : ApplicationFeature<Application, Configuration, GlobalInboundInterceptor> {
        override val key: AttributeKey<GlobalInboundInterceptor> = AttributeKey("InboundChecker")

        val InboundCheckerPhase = PipelinePhase("InboundCheckerPhase")

        private val intercepting: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit = I@{
            val feature = application.featureOrNull(GlobalInboundInterceptor) ?: return@I
            application.log.debug("InboundChecker: intercepting - {} interceptors", feature.interceptors.size)
            val context = InterceptContext()
            for (interceptor in feature.interceptors) {
                interceptor(context, this)
                if (context.finished || context.terminate) break
            }
            if (context.terminate) finish()
        }

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): GlobalInboundInterceptor {
            val configuration = Configuration().apply(configure)
            pipeline.insertPhaseBefore(ApplicationCallPipeline.Features, InboundCheckerPhase)
            pipeline.intercept(InboundCheckerPhase, intercepting)
            return GlobalInboundInterceptor(configuration)
        }
    }
}