package com.shinonometn.koemans.coroutine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Do work in IO threads.
 */
suspend fun <T> background(executor: Executor = Dispatchers.IO.asExecutor(), block: () -> T): T = suspendCoroutine {
    executor.execute {
        try {
            it.resume(block())
        } catch (e: Exception) {
            it.resumeWithException(e)
        }
    }
}

/** Execute blocking code on executor, suspend current coroutine. */
suspend fun <T> Executor.executeSuspended(block: () -> T): T = background(this, block)

/**
 * If job is finish running
 * @return is finished
 */
val Job.isDead: Boolean
    get() = isCancelled || isCompleted