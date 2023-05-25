/*
 * Copyright 2017 Google LLC
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

package app.tivi.home.discover

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.EpisodeWithSeasonWithShow
import app.tivi.data.compoundmodels.PopularEntryWithShow
import app.tivi.data.compoundmodels.RecommendedEntryWithShow
import app.tivi.data.compoundmodels.TrendingEntryWithShow
import app.tivi.data.models.TraktUser
import app.tivi.data.traktauth.TraktAuthState
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class DiscoverUiState(
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
) : CircuitUiState {
    val refreshing: Boolean
        get() = trendingRefreshing || popularRefreshing || recommendedRefreshing
}

sealed interface DiscoverUiEvent : CircuitUiEvent {
    data class Refresh(val fromUser: Boolean = false) : DiscoverUiEvent
    data class ClearMessage(val id: Long) : DiscoverUiEvent
    object OpenAccount : DiscoverUiEvent
    object OpenPopularShows : DiscoverUiEvent
    object OpenRecommendedShows : DiscoverUiEvent
    object OpenTrendingShows : DiscoverUiEvent
    data class OpenShowDetails(val showId: Long, val seasonId: Long? = null, val episodeId: Long? = null) : DiscoverUiEvent
}
