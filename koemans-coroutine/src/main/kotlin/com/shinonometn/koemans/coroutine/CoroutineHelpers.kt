package com.shinonometn.koemans.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun <T> background(executor: Executor = Dispatchers.IO.asExecutor(), block: () -> T): T = suspendCoroutine {
    executor.execute {
        try {
            it.resume(block())
        } catch (e: Exception) {
            it.resumeWithException(e)
        }
    }
}