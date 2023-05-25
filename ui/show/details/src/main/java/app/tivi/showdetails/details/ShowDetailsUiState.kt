/*
 * Copyright 2018 Google LLC
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

package app.tivi.showdetails.details

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.ActionDate
import app.tivi.data.models.TiviShow
import app.tivi.data.views.ShowsWatchStats
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class ShowDetailsUiState(
    val isFollowed: Boolean = false,
    val show: TiviShow = TiviShow.EMPTY_SHOW,
    val relatedShows: List<RelatedShowEntryWithShow> = emptyList(),
    val nextEpisodeToWatch: EpisodeWithSeason? = null,
    val watchStats: ShowsWatchStats? = null,
    val seasons: List<SeasonWithEpisodesAndWatches> = emptyList(),
    val refreshing: Boolean = false,
    val message: UiMessage? = null,
    val eventSink: (ShowDetailsUiEvent) -> Unit,
) : CircuitUiState

sealed interface ShowDetailsUiEvent : CircuitUiEvent {
    data class ClearMessage(val id: Long) : ShowDetailsUiEvent
    data class Refresh(val fromUser: Boolean = true) : ShowDetailsUiEvent
    object ToggleShowFollowed : ShowDetailsUiEvent
    data class MarkSeasonWatched(
        val seasonId: Long,
        val onlyAired: Boolean = false,
        val date: ActionDate = ActionDate.NOW,
    ) : ShowDetailsUiEvent

    data class MarkSeasonUnwatched(val seasonId: Long) : ShowDetailsUiEvent

    data class UnfollowSeason(val seasonId: Long) : ShowDetailsUiEvent

    data class FollowSeason(val seasonId: Long) : ShowDetailsUiEvent
    data class UnfollowPreviousSeasons(val seasonId: Long) : ShowDetailsUiEvent

    data class OpenSeason(val seasonId: Long) : ShowDetailsUiEvent
    data class OpenShowDetails(val showId: Long) : ShowDetailsUiEvent
    data class OpenEpisodeDetails(val episodeId: Long) : ShowDetailsUiEvent
    object NavigateBack : ShowDetailsUiEvent
}
