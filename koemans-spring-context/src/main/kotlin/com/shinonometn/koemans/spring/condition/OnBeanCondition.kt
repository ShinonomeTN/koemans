package com.shinonometn.koemans.spring.condition

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.ConfigurationCondition
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.type.AnnotatedTypeMetadata

@Order(Ordered.LOWEST_PRECEDENCE)
class OnBeanCondition : ConfigurationCondition {
    private val logger = LoggerFactory.getLogger("SpringContext.Conditional")

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val beanFactory = context.beanFactory ?: return run {
            logger.warn("Could not evaluate @OnBeanCondition due to no BeanFactory.")
            false
        }

        val annotation = metadata.getAnnotationAttributes(WithConditionOnBean::class.java.name)!!

        val beanClasses = (annotation["classes"] as Array<*>).filterIsInstance(Class::class.java)
        val classBeanResult = if (beanClasses.isNotEmpty()) {
            logger.debug("Checking if the current context has the bean classes: ${beanClasses.joinToString(",") { it.name }}")
            // Resolve classes
            val beanClassesDefined = beanFactory.beanDefinitionNames.map {
                beanFactory.getBeanDefinition(it).beanClassName
            }.toSet()

            beanClasses.all { beanClassesDefined.contains(it.name) }
        } else true

        val beanNames = (annotation["names"] as Array<*>).filterIsInstance<String>()
        val nameBeanResult = if (beanNames.isNotEmpty()) {
            val beanNamesDefined = beanFactory.beanDefinitionNames.toSet()
            beanNames.all { beanNamesDefined.contains(it) }
        } else true

        return classBeanResult && nameBeanResult
    }

    override fun getConfigurationPhase(): ConfigurationCondition.ConfigurationPhase {
        return ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN
    }
}