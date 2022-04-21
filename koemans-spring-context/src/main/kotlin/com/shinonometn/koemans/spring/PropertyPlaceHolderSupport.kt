package com.shinonometn.koemans.spring

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

fun SpringContextConfiguration.propertySourcePlaceholderSupport() {
    additionalActions {
        registerBean("propertySourcesPlaceholderConfigurer", PropertySourcesPlaceholderConfigurer())
    }
}