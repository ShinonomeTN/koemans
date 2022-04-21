package com.shinonometn.koemans.spring.context

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

@ComponentScan("com.shinonometn.koemans.spring.context")
open class TestApplicationAutoConfiguration {

    @Value("\${application.title}")
    lateinit var applicationTitle: String
        private set
}

@Component
class TestPropertySourcePlaceHolder(@Value("\${application.number1}") val number1: Int) {

    @Value("\${application.boolean2}")
    var boolean2 = false
}