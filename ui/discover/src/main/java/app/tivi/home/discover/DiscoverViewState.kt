// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.discover

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.EpisodeWithSeasonWithShow
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState

@Immutable
data class DiscoverViewState(
    val user: TraktUser? = null,
    val authState: TraktAuthState = TraktAuthState.LOGGED_OUT,
    val trendingItems: List<TrendingEntryWithShow> = emptyList(),
    val trendingRefreshing: Boolean = false,
    val popularItems: List<PopularEntryWithShow> = emptyList(),
    val popularRefreshing: Boolean = false,
    val recommendedItems: List<RecommendedEntryWithShow> = emptyList(),
    val recommendedRefreshing: Boolean = false,
    val nextEpisodeWithShowToWatch: EpisodeWithSeasonWithShow? = null,
    val message: UiMessage? = null,
    val eventSink: (DiscoverUiEvent) -> Unit,
) {
    val refreshing: Boolean
        get() = trendingRefreshing || popularRefreshing || recommendedRefreshing
}

sealed interface DiscoverUiEvent {
    data class Refresh(val fromUser: Boolean = false) : DiscoverUiEvent
    data class ClearMessage(val id: Long) : DiscoverUiEvent
}
