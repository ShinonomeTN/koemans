package com.shinonometn.koemans.utils

fun ByteArray.toLong() : Long {
    var acc = 0L
    var c = 0
    while(c < 8 && c < size) {
        acc = acc shl 8
        acc += this[c]
        c++
    }
    return acc
}

/** Convert a Long to byte array */
fun Long.toByteArray() : ByteArray {
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

/** Read an Int from a byte array*/
fun ByteArray.toInt() : Int {
    var acc = 0
    var c = 0
    while(c < 4 && c < size) {
        acc = acc shl 8
        acc += this[c]
        c++
    }
    return acc
}

/** Convert an Int to byte array */
fun Int.toByteArray() : ByteArray {
    // 00 00 00 00
    return byteArrayOf(
        (this shr 24).toByte(),
        (this shr 16).toByte(),
        (this shr 8).toByte(),
        this.toByte()
    )
}