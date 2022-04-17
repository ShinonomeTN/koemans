package com.shinonometn.koemans.web.spring.conditional

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ComponentScan

@ComponentScan("com.shinonometn.koemans.web.spring.conditional")
open class ConditionalTestAutoConfiguration {
    private val logger = LoggerFactory.getLogger("ConditionalTestAutoConfiguration")

    init {
        logger.info("ConditionalTestAutoConfiguration is loaded")
    }
}