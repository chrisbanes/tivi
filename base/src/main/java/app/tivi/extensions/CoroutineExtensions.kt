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

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

suspend fun <A, B> Collection<A>.parallelForEach(
    concurrency: Int = 10,
    block: suspend (A) -> B
): Unit = coroutineScope {
    val semaphore = Channel<Unit>(concurrency)
    forEach { item ->
        launch {
            semaphore.send(Unit) // Acquire concurrency permit
            block(item)
            semaphore.receive() // Release concurrency permit
        }
    }
}
