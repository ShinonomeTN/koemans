package com.shinonometn.koemans.web.spring

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

fun SpringContextConfiguration.propertySourcePlaceholderSupport() {
    additionalActions {
        registerBean("propertySourcesPlaceholderConfigurer", PropertySourcesPlaceholderConfigurer())
    }
}