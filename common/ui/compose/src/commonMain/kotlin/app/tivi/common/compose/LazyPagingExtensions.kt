// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import app.cash.paging.CombinedLoadStates
import app.cash.paging.LoadStateError
import app.cash.paging.PagingData
import app.cash.paging.cachedIn
import com.slack.circuit.retained.rememberRetained
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

inline fun CombinedLoadStates.appendErrorOrNull(): UiMessage? {
  return (append as? LoadStateError)?.let { UiMessage(it.error) }
}

inline fun CombinedLoadStates.prependErrorOrNull(): UiMessage? {
  return (prepend as? LoadStateError)?.let { UiMessage(it.error) }
}

inline fun CombinedLoadStates.refreshErrorOrNull(): UiMessage? {
  return (refresh as? LoadStateError)?.let { UiMessage(it.error) }
}

@Composable
inline fun <T : Any> Flow<PagingData<T>>.rememberRetainedCachedPagingFlow(
  scope: CoroutineScope = rememberRetainedCoroutineScope(),
): Flow<PagingData<T>> = rememberRetained(this, scope) { cachedIn(scope) }
