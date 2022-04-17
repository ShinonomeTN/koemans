package com.shinonometn.koemans.web.spring.conditional

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.type.AnnotatedTypeMetadata

@Order(Ordered.LOWEST_PRECEDENCE)
class OnBeanCondition : Condition {
    private val logger = LoggerFactory.getLogger("SpringContext.Conditional")

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val beanClasses = (metadata.getAnnotationAttributes(WithConditionOnBean::class.java.name)!!["value"] as Array<*>)
            .filterIsInstance(Class::class.java)

        logger.debug("Checking if the current context has the bean classes: ${beanClasses.joinToString(","){ it.name }}")
        
        val beanFactory = context.beanFactory ?: return run {
            logger.warn(
                "Could not evaluate @OnBeanCondition for [{}] due to no BeanFactory.",
                beanClasses.joinToString(",") { it.name }
            )
            false
        }

        return beanClasses.all { beanFactory.containsBeanDefinition(it.name) }
    }
}