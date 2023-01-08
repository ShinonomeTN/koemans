package io.noxpay.framework.spring.web.event

import io.ktor.application.*
import org.springframework.context.ApplicationEvent

class KtorApplicationStoppedEvent(val application : Application) : ApplicationEvent(application)