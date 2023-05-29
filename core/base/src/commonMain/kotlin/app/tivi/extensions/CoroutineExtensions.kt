// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

fun <T, R> Flow<T?>.flatMapLatestNullable(transform: suspend (value: T) -> Flow<R>): Flow<R?> {
    return flatMapLatest { if (it != null) transform(it) else flowOf(null) }
}

fun <T, R> Flow<T?>.mapNullable(transform: suspend (value: T) -> R): Flow<R?> {
    return map { if (it != null) transform(it) else null }
}
