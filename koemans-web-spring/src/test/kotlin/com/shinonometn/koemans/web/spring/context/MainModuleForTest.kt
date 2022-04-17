package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.SpringContext
import com.shinonometn.koemans.web.spring.installSpringRoutingConfigurations
import com.shinonometn.koemans.web.spring.propertySourcePlaceholderSupport
import com.shinonometn.koemans.web.spring.useHoconPropertySource
import io.ktor.application.*
import io.ktor.routing.*
import org.springframework.core.io.ClassPathResource

fun Application.mainTestModule() {
    install(SpringContext) {
        annotationDriven(TestApplicationAutoConfiguration::class.java) {
            propertySourcePlaceholderSupport()
            useHoconPropertySource(null, ClassPathResource("application.hocon"))
        }
    }

    routing {
        installSpringRoutingConfigurations()
    }
}