/*
 * Copyright 2022 Google LLC
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

package app.tivi.home.library

import androidx.paging.compose.LazyPagingItems
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

data class LibraryUiState(
    val items: LazyPagingItems<LibraryShow>,
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT,
    val isLoading: Boolean = false,
    val filterActive: Boolean = false,
    val filter: String? = null,
    val availableSorts: List<SortOption> = emptyList(),
    val sort: SortOption = SortOption.LAST_WATCHED,
    val message: UiMessage? = null,
    val followedShowsIncluded: Boolean = false,
    val watchedShowsIncluded: Boolean = false,
    val eventSink: (LibraryUiEvent) -> Unit,
) : CircuitUiState

sealed interface LibraryUiEvent : CircuitUiEvent {
    data class ClearMessage(val id: Long) : LibraryUiEvent
    data class Refresh(val fromUser: Boolean = false) : LibraryUiEvent
    data class ChangeFilter(val filter: String?) : LibraryUiEvent
    data class ChangeSort(val sort: SortOption) : LibraryUiEvent
    object ToggleFollowedShowsIncluded : LibraryUiEvent
    object ToggleWatchedShowsIncluded : LibraryUiEvent
    object OpenAccount : LibraryUiEvent
    data class OpenShowDetails(val showId: Long) : LibraryUiEvent
}
