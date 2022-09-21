package com.shinonometn.koemans.utils

import org.junit.Assert.*
import org.junit.Test

class LocationUtilsKtTest {

    @Test
    fun `Test normal paths`() {
        listOf(
            "file.name" to "file.name",
            "file" to "file",
            "path/file" to "file",
            "./file" to "file",
            "./path/file" to "file",
            "/path" to "path",
            "/path/" to "path",
            "/path/file" to "file",
            "/path/to/file" to "file",
            "/path/file?" to "file",
            "/path/file?with" to "file",
            "/path/file?with=parameter&s" to "file",
        ).forEach { (path, expected) -> assertEquals("Case '$path' test failed." , expected,resolvePathBaseName(path)) }
    }

    @Test
    fun `Test resolve base name`() {
        listOf(
            "file" to "file",
            "/file" to "file",
            "/file/file" to "file",
            "/file/file/" to "file",
            "protocol:" to "",
            "protocol:/" to "",
            "protocol://" to "",
            "anyProtocol://domain/file" to "file",
            "notAnNormalUrl:file" to "file",
            "notAnNormalUrl:/file" to "file",
            "protocol://domain.name" to "domain.name",
            "http://domain/name?" to "name",
            "http://domain/name?v" to "name",
            "http://domain/name?v=a" to "name",
            "http://domain/name?v=a&" to "name",
        ).forEach { (value, expected) -> assertEquals("Test case '$value' failed.",expected, resolvePathBaseName(value)) }
    }
}