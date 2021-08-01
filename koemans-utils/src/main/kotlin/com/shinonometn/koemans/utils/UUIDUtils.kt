package com.shinonometn.koemans.utils

import java.nio.ByteBuffer
import java.util.*

fun randomUUIDBytes(): ByteArray = ByteBuffer.wrap(ByteArray(16)).let {
    val uuid = UUID.randomUUID()

    it.putLong(uuid.mostSignificantBits)
    it.putLong(uuid.leastSignificantBits)

    it.array()
}

val UUID.high: Long
    get() = this.mostSignificantBits

val UUID.low: Long
    get() = this.leastSignificantBits

fun randomUUID(): UUID = UUID.randomUUID()

//fun randomUUIDString() = Hex.encodeHexString(DigestUtils.md5(randomUUIDBytes())).toLowerCase()