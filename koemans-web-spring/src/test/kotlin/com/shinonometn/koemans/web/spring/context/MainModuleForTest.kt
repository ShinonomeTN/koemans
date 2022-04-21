package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.configuration.configureBySpring
import com.shinonometn.koemans.web.spring.propertySourcePlaceholderSupport
import com.shinonometn.koemans.web.spring.useHoconPropertySource
import io.ktor.application.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.ComponentScan
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component

fun Application.mainTestModule() {
    configureBySpring {
        annotationDriven(TestApplicationAutoConfiguration::class.java) {
            propertySourcePlaceholderSupport()
            useHoconPropertySource(null, ClassPathResource("context.hocon"))
        }
    }
}

@ComponentScan("com.shinonometn.koemans.web.spring.context")
open class TestApplicationAutoConfiguration {

    @Value("\${application.title}")
    lateinit var applicationTitle: String
        private set
}

@Component
class TestPropertySourcePlaceHolder(@Value("\${application.number}") val number : Int)