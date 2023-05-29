// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
