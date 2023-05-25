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

package app.tivi.episodedetails

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.models.Episode
import app.tivi.data.models.EpisodeWatchEntry
import app.tivi.data.models.Season
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class EpisodeDetailsUiState(
    val season: Season? = null,
    val episode: Episode? = null,
    val watches: List<EpisodeWatchEntry> = emptyList(),
    val canAddEpisodeWatch: Boolean = false,
    val refreshing: Boolean = false,
    val message: UiMessage? = null,
    val eventSink: (EpisodeDetailsUiEvent) -> Unit,
) : CircuitUiState

sealed interface EpisodeDetailsUiEvent : CircuitUiEvent {
    data class Refresh(val fromUser: Boolean = false) : EpisodeDetailsUiEvent
    data class RemoveWatchEntry(val id: Long) : EpisodeDetailsUiEvent
    data class ClearMessage(val id: Long) : EpisodeDetailsUiEvent
    object RemoveAllWatches : EpisodeDetailsUiEvent
    object OpenTrackEpisode : EpisodeDetailsUiEvent
    object NavigateUp : EpisodeDetailsUiEvent
}
