/*
 * Copyright 2018 Google, Inc.
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

package me.banes.chris.tivi.extensions

import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.async
import kotlin.coroutines.experimental.CoroutineContext

suspend fun <A, B> Collection<A>.parallelMap(
    context: CoroutineContext = DefaultDispatcher,
    block: suspend (A) -> B
): Collection<B> {
    return map {
        async(context) {
            block(it)
        }
    }.map {
        it.await()
    }
}

suspend fun <A, B> Collection<A>.parallelForEach(
    context: CoroutineContext = DefaultDispatcher,
    block: suspend (A) -> B
) {
    return map {
        async(context) {
            block(it)
        }
    }.forEach {
        it.await()
    }
}