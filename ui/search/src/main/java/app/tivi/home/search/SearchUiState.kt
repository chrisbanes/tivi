/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
