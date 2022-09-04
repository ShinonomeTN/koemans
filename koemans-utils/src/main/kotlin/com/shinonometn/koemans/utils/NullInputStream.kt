package com.shinonometn.koemans.utils

import java.io.InputStream

object NullInputStream : InputStream() {
    override fun read(): Int = -1
    override fun available(): Int = 0
}

fun nullInputStream() = NullInputStream