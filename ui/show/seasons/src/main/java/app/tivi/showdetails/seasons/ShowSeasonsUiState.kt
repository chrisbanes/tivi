/*
 * Copyright 2021 Google LLC
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

    data class OpenSeason(val seasonId: Long) : ShowSeasonsUiEvent
    data class OpenShowDetails(val showId: Long) : ShowSeasonsUiEvent
    data class OpenEpisodeDetails(val episodeId: Long) : ShowSeasonsUiEvent
    object NavigateBack : ShowSeasonsUiEvent
}
