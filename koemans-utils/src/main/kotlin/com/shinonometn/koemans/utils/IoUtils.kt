package com.shinonometn.koemans.utils

import java.io.InputStream
import java.io.OutputStream

/**
 * An input stream simply only return 0 and
 * has infinity bytes
 */
object NullInputStream : InputStream() {
    override fun read(): Int = 0
    override fun available(): Int = Int.MAX_VALUE
}

/**
 * Giv an input stream simply only return 0 and
 * has infinity bytes
 */
fun nullInputStream() = NullInputStream

/**
 * An input stream contains nothing
 */
object EmptyInputStream : InputStream() {
    override fun read(): Int = -1
    override fun available(): Int = 0
}

/**
 * Give an input stream contains nothing
 */
fun emptyInputStream() = EmptyInputStream

/**
 * An OutputStream simply discard everything
 */
object NullOutputStream : OutputStream() {
    override fun write(b: ByteArray) {}
    override fun write(b: ByteArray, off: Int, len: Int) {}
    override fun write(i: Int) {}
}

/**
 * Give an OutputStream simply discard everything
 */
fun nullOutputStream() = NullOutputStream