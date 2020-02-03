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
import com.dropbox.android.external.store4.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.first

suspend fun <Key : Any, Output : Any> Store<Key, Output>.fetchIfEmpty(key: Key) {
    val localValue = stream(StoreRequest.cached(key, refresh = false))
        .filterNot { it is StoreResponse.Loading }
        .first()
    if (localValue !is StoreResponse.Data) {
        // If we don't have a valid Data response, just get it
        get(key)
    }
}

suspend fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false
): Output {
    return if (forceFresh) {
        get(key)
    } else {
        fresh(key)
    }
}

suspend fun <Key : Any, Output : Any> Store<Key, Output>.cached(key: Key): Output? {
    return stream(StoreRequest.cached(key, refresh = false))
        .filterNot { it is StoreResponse.Loading }
        .first()
        .dataOrNull()
}

suspend fun <Output : Any> Flow<StoreResponse<Output>>.requireFirstData(): Output? {
    return filterNot { it is StoreResponse.Loading }.first().requireData()
}
