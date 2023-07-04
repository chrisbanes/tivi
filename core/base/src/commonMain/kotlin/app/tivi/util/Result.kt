// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.CancellationException

inline fun Result<*>.onException(
    block: (Throwable) -> Unit,
) {
    val e = exceptionOrNull()
    when {
        e is CancellationException -> throw e
        e != null -> block(e)
    }
}
