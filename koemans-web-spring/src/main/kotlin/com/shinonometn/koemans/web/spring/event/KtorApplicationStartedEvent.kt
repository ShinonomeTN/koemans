package io.noxpay.framework.spring.web.event

import io.ktor.application.*
import org.springframework.context.ApplicationEvent

class KtorApplicationStartedEvent(val application : Application) : ApplicationEvent(application)