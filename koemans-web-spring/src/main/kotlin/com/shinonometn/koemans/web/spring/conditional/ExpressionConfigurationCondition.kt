package com.shinonometn.koemans.web.spring.conditional

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.context.expression.StandardBeanExpressionResolver
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.type.AnnotatedTypeMetadata

/**
 * Reference: [org.springframework.boot.autoconfigure.condition.ConditionalOnExpression]
 */
@Order(Ordered.LOWEST_PRECEDENCE - 20)
class ExpressionConfigurationCondition : Condition {
    private val logger = LoggerFactory.getLogger("SpringContext.Conditional")

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        var expression = metadata.getAnnotationAttributes(WithConditionExpression::class.java.name)!!["value"] as String

        val beanFactory = context.beanFactory ?: return run {
            logger.warn("No BeanFactory, cannot evaluate expression '$expression'.")
            false
        }

        logger.debug("Preparing to evaluate expression '{}'.", expression)
        val resolver = beanFactory.beanExpressionResolver ?: StandardBeanExpressionResolver()
        val expressionContext = BeanExpressionContext(beanFactory, null)
        expression = context.environment.resolvePlaceholders(expression)
        expression = wrapIfNecessary(expression)
        logger.debug("After property resolution: '{}'.", expression)

        val result = resolver.evaluate(expression, expressionContext)

        return (result != null && result as Boolean)
    }

    private fun wrapIfNecessary(expression: String): String {
        if (!expression.startsWith("#{")) return "#{$expression}"
        return expression
    }
}