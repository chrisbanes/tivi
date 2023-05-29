// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.search

import app.tivi.api.UiMessage
import app.tivi.data.models.TiviShow

data class SearchViewState(
    val query: String = "",
    val searchResults: List<TiviShow> = emptyList(),
    val refreshing: Boolean = false,
    val message: UiMessage? = null,
    val eventSink: (SearchUiEvent) -> Unit,
)

sealed interface SearchUiEvent {
    data class ClearMessage(val id: Long) : SearchUiEvent
    data class UpdateQuery(val query: String) : SearchUiEvent
}
