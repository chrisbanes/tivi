// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.seasons

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.TiviShow
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class ShowSeasonsUiState(
    val show: TiviShow = TiviShow.EMPTY_SHOW,
    val seasons: List<SeasonWithEpisodesAndWatches> = emptyList(),
    val refreshing: Boolean = false,
    val message: UiMessage? = null,
    val initialSeasonId: Long? = null,
    val eventSink: (ShowSeasonsUiEvent) -> Unit,
) : CircuitUiState

sealed interface ShowSeasonsUiEvent : CircuitUiEvent {
    data class ClearMessage(val id: Long) : ShowSeasonsUiEvent
    data class Refresh(val fromUser: Boolean = true) : ShowSeasonsUiEvent
    data class OpenEpisodeDetails(val episodeId: Long) : ShowSeasonsUiEvent
    object NavigateBack : ShowSeasonsUiEvent
}
