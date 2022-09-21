package com.shinonometn.koemans.utils

/* For Long */

/**
 * Convert a byte array to long.
 * It takes first 8 bytes or all bytes if lesser than 8.
 */
fun ByteArray.toLong(): Long {
    var acc = 0L
    var c = 0
    while (c < 8 && c < size) {
        acc = acc shl 8
        acc += this[c]
        c++
    }
    return acc
}

/** Convert a given range of byte to long */
fun ByteArray.toLong(offset: Int = 0, byteCount: Int = Long.SIZE_BYTES): Long {
    if ((size - (byteCount + offset)) < 0) throw ArrayIndexOutOfBoundsException()
    var long = 0L
    for (index in 0 until byteCount) long = (long shl 8) or (this[index + offset].toUByte().toLong())
    return long
}

/** Convert a given range of byte to long */
fun ByteArray.toLong(range : IntRange) : Long {
    val offset = range.first
    val byteCount = 1 + (range.last - range.first)
    return toLong(offset, byteCount)
}

/** Convert a Long to byte array */
fun Long.toByteArray(): ByteArray {
    // 00 00 00 00 00 00 00 00
    return byteArrayOf(
        (this shr 56).toByte(),
        (this shr 48).toByte(),
        (this shr 40).toByte(),
        (this shr 32).toByte(),
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}

/* For Integer */

/** Read an Int from a byte array,
 * reading first 4 bytes or all if not enough
 */
fun ByteArray.toInt(): Int {
    var acc = 0
    var c = 0
    while (c < 4 && c < size) {
        acc = acc shl 8
        acc += this[c]
        c++
    }
    return acc
}

/** Convert a given range of byte to int */
fun ByteArray.toInt(offset: Int = 0, byteCount: Int = Int.SIZE_BYTES): Int {
    if ((size - (byteCount + offset)) < 0) throw ArrayIndexOutOfBoundsException()
    var int = 0
    for (index in 0 until byteCount) int = (int shl 8) or (this[index + offset].toUByte().toInt())
    return int
}

/** Convert a given range of byte to int */
fun ByteArray.toInt(range : IntRange) : Int {
    val offset = range.first
    val byteCount = 1 + (range.last - range.first)
    return toInt(offset, byteCount)
}

/** Convert an Int to byte array */
fun Int.toByteArray(): ByteArray {
    // 00 00 00 00
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}