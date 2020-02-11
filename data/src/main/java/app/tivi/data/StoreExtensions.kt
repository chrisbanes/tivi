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

import com.dropbox.android.external.store4.ResponseOrigin
import com.dropbox.android.external.store4.Store
import com.dropbox.android.external.store4.StoreRequest
import com.dropbox.android.external.store4.StoreResponse
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false,
    crossinline doFreshIf: (Output) -> Boolean = { false }
): Output {
    val request = if (forceFresh) {
        StoreRequest.fresh(key)
    } else {
        StoreRequest.cached(key, refresh = true)
    }
    return stream(request).filter { result ->
        when (result) {
            is StoreResponse.Error -> true // let errors pass
            is StoreResponse.Data -> {
                result.origin == ResponseOrigin.Fetcher || !doFreshIf(result.requireData())
            }
            else -> false // drop anything else
        }
    }.first().requireData()
}

/**
 * A wrapper around [fetch] which supports non-nullable collection outputs.
 * Primarily it checks for empty collections
 */
suspend inline fun <Key : Any, Output : Collection<Any>> Store<Key, Output>.fetchCollection(
    key: Key,
    forceFresh: Boolean = false,
    crossinline doFreshIf: (Output) -> Boolean = { false }
): Output {
    return fetch(key, forceFresh = forceFresh) { output ->
        output.isEmpty() || doFreshIf(output)
    }
}

suspend fun <Key : Any, Output : Any> Store<Key, Output>.cachedOnly(key: Key): Output? {
    return stream(StoreRequest.cached(key, refresh = false))
        .filterNot { it is StoreResponse.Loading }
        .first()
        .dataOrNull()
}
