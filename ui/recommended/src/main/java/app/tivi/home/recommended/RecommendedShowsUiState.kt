// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.recommended

import androidx.compose.runtime.Stable
import app.cash.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable // 'only' stable due to LazyPagingItems
data class RecommendedShowsUiState(
    val items: LazyPagingItems<RecommendedEntryWithShow>,
    val eventSink: (RecommendedShowsUiEvent) -> Unit,
) : CircuitUiState

sealed interface RecommendedShowsUiEvent : CircuitUiEvent {
    data class OpenShowDetails(val showId: Long) : RecommendedShowsUiEvent
    object NavigateUp : RecommendedShowsUiEvent
}
