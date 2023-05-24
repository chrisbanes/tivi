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

package app.tivi.home.upnext

import androidx.paging.compose.LazyPagingItems
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.models.SortOption
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState

data class UpNextViewState(
    val items: LazyPagingItems<UpNextEntry>,
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT,
    val isLoading: Boolean = false,
    val availableSorts: List<SortOption> = emptyList(),
    val sort: SortOption = SortOption.LAST_WATCHED,
    val message: UiMessage? = null,
    val followedShowsOnly: Boolean = false,
    val eventSink: (UpNextUiEvent) -> Unit,
)

sealed interface UpNextUiEvent {
    data class ClearMessage(val id: Long) : UpNextUiEvent
    data class Refresh(val fromUser: Boolean = false) : UpNextUiEvent
    data class ChangeSort(val sort: SortOption) : UpNextUiEvent
    object ToggleFollowedShowsOnly : UpNextUiEvent
}
