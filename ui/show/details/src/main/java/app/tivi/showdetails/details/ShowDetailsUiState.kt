// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

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
