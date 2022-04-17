package com.shinonometn.koemans.web.spring.conditional

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@WithConditionExpression("\${test.value3:false}")
@Component
class TestConditionalBeanExists {
    private val logger = LoggerFactory.getLogger(TestConditionalBeanExists::class.java)

    init {
        logger.info("TestConditionalBeanExists is created")
    }
}