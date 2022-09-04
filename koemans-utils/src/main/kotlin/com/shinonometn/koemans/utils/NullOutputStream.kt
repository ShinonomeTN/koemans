package com.shinonometn.koemans.utils

import java.io.OutputStream

object NullOutputStream : OutputStream() {
    override fun write(b: ByteArray) {}
    override fun write(b: ByteArray, off: Int, len: Int) {}
    override fun write(i: Int) {}
}

fun nullOutputStream() = NullOutputStream