/*
 * Copyright 2020 Google LLC
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

package app.tivi.data

import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import com.dropbox.android.external.store4.fresh
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false
): Output = when {
    // If we're forcing a fresh fetch, do it now
    forceFresh -> fresh(key)
    else -> cachedOnly(key) ?: fresh(key)
}

@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false,
    doFreshIf: suspend (Output) -> Boolean
): Output = when {
    // If we're forcing a fresh fetch, do it now
    forceFresh -> fresh(key)
    else -> {
        // Else we'll check the current cached value
        val cached = cachedOnly(key)
        if (cached == null || doFreshIf(cached)) {
            // Our cached value isn't valid, do a fresh fetch
            fresh(key)
        } else {
            // We have a current cached value
            cached
        }
    }
}

suspend inline fun <Key : Any, Output : Collection<Any>> Store<Key, Output>.fetchCollection(
    key: Key,
    forceFresh: Boolean = false
): Output = fetch(key, forceFresh = forceFresh) {
    it.isEmpty()
}

/**
 * A wrapper around [fetch] which supports non-nullable collection outputs.
 * Primarily it checks for empty collections
 */
@Suppress("REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE")
suspend inline fun <Key : Any, Output : Collection<Any>> Store<Key, Output>.fetchCollection(
    key: Key,
    forceFresh: Boolean = false,
    crossinline doFreshIf: suspend (Output) -> Boolean
): Output = fetch(key, forceFresh = forceFresh) {
    it.isEmpty() || doFreshIf(it)
}

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.cachedOnly(key: Key): Output? {
    return stream(StoreRequest.cached(key, refresh = false))
        .filterNot { it is StoreResponse.Loading }
        .first()
        .dataOrNull()
}
