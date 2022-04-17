package com.shinonometn.koemans.web.spring.context

import com.shinonometn.koemans.web.spring.RoutingProvider
import com.shinonometn.koemans.web.spring.injectRoute
import com.shinonometn.koemans.web.spring.injectRouteGroup
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan

@ComponentScan("com.shinonometn.koemans.web.spring.context")
open class TestApplicationAutoConfiguration {

    @Value("\${application.title}")
    lateinit var applicationTitle: String
        private set

    @Bean
    open fun rootRouting() = RoutingProvider {
        injectRouteGroup("default_routing_group")
        injectRoute<TestRouteConfigClass>()
    }
}