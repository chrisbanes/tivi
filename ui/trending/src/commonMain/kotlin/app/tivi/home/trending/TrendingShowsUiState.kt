// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.trending

import androidx.compose.runtime.Stable
import app.cash.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable // 'only' stable due to LazyPagingItems
data class TrendingShowsUiState(
    val items: LazyPagingItems<TrendingEntryWithShow>,
    val eventSink: (TrendingShowsUiEvent) -> Unit,
) : CircuitUiState

sealed interface TrendingShowsUiEvent : CircuitUiEvent {
    data class OpenShowDetails(val showId: Long) : TrendingShowsUiEvent
    object NavigateUp : TrendingShowsUiEvent
}
