// Copyright 2023, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.DEFAULT_CONCURRENCY
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
suspend fun <T> Collection<T>.parallelForEach(
  concurrency: Int = DEFAULT_CONCURRENCY,
  block: suspend (value: T) -> Unit,
) {
  asFlow().flatMapMerge(concurrency = concurrency) { item ->
    flow {
      block(item)
      emit(Unit)
    }
  }.collect()
}
