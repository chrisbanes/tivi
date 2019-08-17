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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

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

val deferreds = ConcurrentHashMap<Any, Deferred<*>>()
val deferredsCleanLaunched = AtomicBoolean()

suspend inline fun <T> asyncOrAwait(
    key: Any,
    crossinline action: suspend CoroutineScope.() -> T
): T = coroutineScope {
    val deferred = deferreds[key]?.takeIf { it.isActive }
            ?: async { action() }
                    .also { deferreds[key] = it }

    if (deferreds.size > 100 && !deferredsCleanLaunched.getAndSet(true)) {
        launch {
            // Remove any complete entries
            deferreds.entries.removeAll { it.value.isCompleted }
            deferredsCleanLaunched.set(false)
        }
    }

    @Suppress("UNCHECKED_CAST")
    deferred.await() as T
}

val jobs = ConcurrentHashMap<Any, Job>()
val jobsCleanLaunched = AtomicBoolean()

suspend inline fun launchOrJoin(
    key: Any,
    crossinline action: suspend CoroutineScope.() -> Unit
) = coroutineScope {
    val job = jobs[key]?.takeIf { it.isActive }
            ?: launch { action() }
                    .also { jobs[key] = it }

    if (jobs.size > 100 && !jobsCleanLaunched.getAndSet(true)) {
        launch {
            // Remove any complete entries
            jobs.entries.removeAll { it.value.isCompleted }
            jobsCleanLaunched.set(false)
        }
    }

    job.join()
}