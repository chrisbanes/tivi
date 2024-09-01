// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.anticipated

import androidx.compose.runtime.Stable
import androidx.paging.compose.LazyPagingItems
import app.tivi.data.compoundmodels.AnticipatedShowEntryWithShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Stable // 'only' stable due to LazyPagingItems
data class AnticipatedShowsUiState(
  val items: LazyPagingItems<AnticipatedShowEntryWithShow>,
  val eventSink: (AnticipatedShowsUiEvent) -> Unit,
) : CircuitUiState

sealed interface AnticipatedShowsUiEvent : CircuitUiEvent {
  data class OpenShowDetails(val showId: Long) : AnticipatedShowsUiEvent
  data object NavigateUp : AnticipatedShowsUiEvent
}
