// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.popular

import androidx.compose.runtime.Stable
import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.PopularEntryWithShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable // 'only' stable due to LazyPagingItems
data class PopularShowsUiState(
    val items: LazyPagingItems<PopularEntryWithShow>,
    val eventSink: (PopularShowsUiEvent) -> Unit,
) : CircuitUiState

sealed interface PopularShowsUiEvent : CircuitUiEvent {
    data class OpenShowDetails(val showId: Long) : PopularShowsUiEvent
    object NavigateUp : PopularShowsUiEvent
}
