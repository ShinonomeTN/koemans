package com.shinonometn.koemans.utils

object CommonRegex {
    val HttpUrl = Regex("^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]")
    val Base64 = Regex("^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?\$")
    val Base64UrlSafe = Regex("^[a-zA-Z0-9_-]+\$")
    val ChinaCellPhoneNumber = Regex("^1\\d{10}$")
}