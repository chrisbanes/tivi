// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.search

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.models.TiviShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class SearchUiState(
    val query: String = "",
    val searchResults: List<TiviShow> = emptyList(),
    val refreshing: Boolean = false,
    val message: UiMessage? = null,
    val eventSink: (SearchUiEvent) -> Unit,
) : CircuitUiState

sealed interface SearchUiEvent : CircuitUiEvent {
    data class ClearMessage(val id: Long) : SearchUiEvent
    data class UpdateQuery(val query: String) : SearchUiEvent
    data class OpenShowDetails(val showId: Long) : SearchUiEvent
}
