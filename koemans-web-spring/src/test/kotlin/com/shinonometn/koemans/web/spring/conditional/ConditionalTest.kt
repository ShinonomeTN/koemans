package com.shinonometn.koemans.web.spring.conditional

import com.shinonometn.koemans.web.spring.HoconPropertySource
import com.shinonometn.koemans.web.spring.find
import org.junit.Test
import org.springframework.beans.factory.NoSuchBeanDefinitionException
import org.springframework.beans.factory.config.BeanExpressionContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.expression.StandardBeanExpressionResolver
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.support.EncodedResource
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ConditionalTest {

    private fun createSpringContext(): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()
        context.environment.propertySources.addFirst(
            HoconPropertySource.buildPropertySourceFrom(
                "conditional",
                EncodedResource(ClassPathResource("conditional.hocon"), "UTF8")
            )
        )
        context.register(ConditionalTestAutoConfiguration::class.java)
        context.refresh()
        context.start()
        return context
    }

    private fun createEmptySpringContext(): AnnotationConfigApplicationContext {
        val context = AnnotationConfigApplicationContext()
        context.environment.propertySources.addFirst(
            HoconPropertySource.buildPropertySourceFrom(
                "conditional",
                EncodedResource(ClassPathResource("conditional.hocon"), "UTF8")
            )
        )
        context.refresh()
        context.start()
        return context
    }

    @Test
    fun `Test conditional on bean`() {
        val context = createSpringContext()
        context.find<TestConditionalOnBean>()
    }

    @Test(expected = NoSuchBeanDefinitionException::class)
    fun `Test conditional on bean not found`() {
        val context = createSpringContext()
        context.find<TestConditionalOnBeanNotLoaded>()
    }

    @Test
    fun `Test evaluation 1`() {
        val context = createEmptySpringContext()
        val resolver = context.beanFactory.beanExpressionResolver ?: StandardBeanExpressionResolver()
        val context1 = BeanExpressionContext(context.beanFactory, null)
        val rawExpression = "#{\${test.value3:false}}"
        val processedExpression = context.environment.resolvePlaceholders(rawExpression)
        val result = resolver.evaluate(processedExpression, context1) as Boolean

        assertNotNull(result)
        assertTrue(result, "test.value3 should be true.")
    }

    @Test
    fun `Test evaluation 2`() {
        val parser = SpelExpressionParser()
        val expression = parser.parseExpression("true")
        val result = expression.getValue(StandardEvaluationContext(), Boolean::class.java)
        assertNotNull(result)
        assertTrue(result, "result should be true.")
    }

    @Test
    fun `Test conditional evaluation 1`() {
        val context = createSpringContext()
        context.find<TestConditionalBeanExists>()
    }

    @Test(expected = NoSuchBeanDefinitionException::class)
    fun `Test conditional evaluation 2`() {
        val context = createSpringContext()
        context.find<TestConditionalBeanNotExists>()
    }
}