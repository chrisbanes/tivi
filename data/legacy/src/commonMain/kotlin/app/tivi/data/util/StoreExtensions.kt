// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.data.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNot
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Store
import org.mobilenativefoundation.store.store5.StoreBuilder
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.impl.extensions.fresh
import org.mobilenativefoundation.store.store5.impl.extensions.get

suspend inline fun <Key : Any, Output : Any> Store<Key, Output>.fetch(
    key: Key,
    forceFresh: Boolean = false,
): Output = when {
    // If we're forcing a fresh fetch, do it now
    forceFresh -> fresh(key)
    else -> get(key)
}

fun <T> Flow<StoreReadResponse<T>>.filterForResult(): Flow<StoreReadResponse<T>> = filterNot {
    it is StoreReadResponse.Loading || it is StoreReadResponse.NoNewData
}

@Suppress("NOTHING_TO_INLINE")
inline fun <Key : Any, Model : Any> storeBuilder(
    fetcher: Fetcher<Key, Model>,
    sourceOfTruth: SourceOfTruth<Key, Model>,
): StoreBuilder<Key, Model> = StoreBuilder.from(fetcher, sourceOfTruth)
