// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.library

import androidx.paging.compose.LazyPagingItems
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState

data class LibraryViewState(
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
)

sealed interface LibraryUiEvent {
    data class ClearMessage(val id: Long) : LibraryUiEvent
    data class Refresh(val fromUser: Boolean = false) : LibraryUiEvent
    data class ChangeFilter(val filter: String?) : LibraryUiEvent
    data class ChangeSort(val sort: SortOption) : LibraryUiEvent
    object ToggleFollowedShowsIncluded : LibraryUiEvent
    object ToggleWatchedShowsIncluded : LibraryUiEvent
}
