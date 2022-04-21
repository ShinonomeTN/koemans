package com.shinonometn.koemans.web.spring.conditional

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component

private val logger = LoggerFactory.getLogger(ConditionalTest::class.java)

@Component
@WithConditionOnBean(TestConditionalBeanNotExists::class)
class TestConditionalOnBeanNotLoaded

@Component
class TestConditionalOnBeanDependency {
    init {
        logger.info("TestConditionalOnBeanDependency is loaded")
    }
}

@Component
@WithConditionOnBean(TestConditionalOnBeanDependency::class)
class TestConditionalOnBean

@Component
@WithConditionExpression("\${test.value4:true}")
class TestConditionalBeanNotExists

@WithConditionExpression("\${test.value3:false}")
@Component
class TestConditionalBeanExists {
    init {
        logger.info("TestConditionalBeanExists is created")
    }
}

@ComponentScan("com.shinonometn.koemans.web.spring.conditional")
open class ConditionalTestAutoConfiguration {
    private val logger = LoggerFactory.getLogger("ConditionalTestAutoConfiguration")

    init {
        logger.info("ConditionalTestAutoConfiguration is loaded")
    }
}