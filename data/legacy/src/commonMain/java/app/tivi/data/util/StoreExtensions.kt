/*
 * Copyright 2023 Google LLC
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

package app.tivi.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreResponse
import org.mobilenativefoundation.store.store5.fresh
import org.mobilenativefoundation.store.store5.get

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false,
): Output = when {
    // If we're forcing a fresh fetch, do it now
    forceFresh -> fresh(key)
    else -> get(key)
}

fun <T> Flow<StoreResponse<T>>.filterForResult(): Flow<StoreResponse<T>> = filterNot {
    it is StoreResponse.Loading || it is StoreResponse.NoNewData
}
