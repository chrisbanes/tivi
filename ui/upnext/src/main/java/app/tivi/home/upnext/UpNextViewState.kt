// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
