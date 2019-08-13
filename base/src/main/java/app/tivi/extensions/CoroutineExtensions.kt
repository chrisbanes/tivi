/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

suspend fun <A, B> Collection<A>.parallelMap(
    concurrency: Int = defaultConcurrency,
    block: suspend (A) -> B
): List<B> = coroutineScope {
    val semaphore = Channel<Unit>(concurrency)
    map { item ->
        async {
            semaphore.send(Unit) // Acquire concurrency permit
            try {
                block(item)
            } finally {
                semaphore.receive() // Release concurrency permit
            }
        }
    }.awaitAll()
}

suspend fun <A> Collection<A>.parallelForEach(
    concurrency: Int = defaultConcurrency,
    block: suspend (A) -> Unit
): Unit = supervisorScope {
    val semaphore = Channel<Unit>(concurrency)
    forEach { item ->
        launch {
            semaphore.send(Unit) // Acquire concurrency permit
            try {
                block(item)
            } finally {
                semaphore.receive() // Release concurrency permit
            }
        }
    }
}

private val defaultConcurrency by lazy(LazyThreadSafetyMode.NONE) {
    Runtime.getRuntime().availableProcessors().coerceAtLeast(3)
}

private val inFlightDeferreds = mutableMapOf<Any, Deferred<*>>()
private val inFlightDeferredsLock = Mutex()

suspend fun <T> asyncOrAwait(key: Any, action: suspend CoroutineScope.() -> T): T = coroutineScope {
    val deferred = inFlightDeferredsLock.withLock {
        inFlightDeferreds[key]?.takeIf { it.isActive }
                ?: async { action() }.also { inFlightDeferreds[key] = it }
    }
    @Suppress("UNCHECKED_CAST")
    deferred.await() as T
}

private val inFlightJobs = mutableMapOf<Any, Job>()
private val inFlightJobsLock = Mutex()

val list = mutableListOf<String>()

suspend fun launchOrJoin(key: Any, action: suspend CoroutineScope.() -> Unit) = coroutineScope {
    val job = inFlightJobsLock.withLock {
        inFlightJobs[key]?.takeIf { it.isActive }
                ?: launch { action() }.also { inFlightJobs[key] = it }
    }
    job.join()
}