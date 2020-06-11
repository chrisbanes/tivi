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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

fun <T, R> Flow<T?>.flatMapLatestNullable(transform: suspend (value: T) -> Flow<R>): Flow<R?> {
    return flatMapLatest { if (it != null) transform(it) else flowOf(null) }
}

fun <T, R> Flow<T?>.mapNullable(transform: suspend (value: T) -> R): Flow<R?> {
    return map { if (it != null) transform(it) else null }
}

val deferreds = ConcurrentHashMap<Any, Deferred<*>>()
val deferredsCleanLaunched = AtomicBoolean()

suspend inline fun <T> asyncOrAwait(
    key: Any,
    crossinline action: suspend CoroutineScope.() -> T
): T = coroutineScope {
    val deferred = deferreds[key]?.takeIf { it.isActive }
        ?: async { action() }.also { deferreds[key] = it }

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

fun <T> delayFlow(timeout: Long, value: T): Flow<T> = flow {
    delay(timeout)
    emit(value)
}
