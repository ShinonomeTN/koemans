package com.shinonometn.koemans.web.spring.conditional

import org.springframework.stereotype.Component

@Component
@WithConditionExpression("\${test.value4:true}")
class TestConditionalBeanNotExists