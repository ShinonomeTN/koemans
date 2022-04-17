package com.shinonometn.koemans.web.spring.context

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TestPropertySourcePlaceHolder(@Value("\${application.number}") val number : Int)