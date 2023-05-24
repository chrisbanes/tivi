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

package app.tivi.episode.track

import androidx.compose.runtime.Immutable
import app.tivi.api.UiMessage
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Immutable
data class EpisodeTrackViewState(
    val season: Season? = null,
    val episode: Episode? = null,
    val showEpisodeInfo: Boolean = true,
    val refreshing: Boolean = false,
    val message: UiMessage? = null,

    val canSubmit: Boolean = false,
    val submitInProgress: Boolean = false,

    val showSetFirstAired: Boolean = false,
    val selectedDate: LocalDate? = null,
    val selectedTime: LocalTime? = null,
    val selectedNow: Boolean = true,

    val shouldDismiss: Boolean = false,

    val eventSink: (EpisodeTrackUiEvent) -> Unit,
)

sealed interface EpisodeTrackUiEvent {
    data class Refresh(val fromUser: Boolean = false) : EpisodeTrackUiEvent
    object Submit : EpisodeTrackUiEvent
    object SelectNow : EpisodeTrackUiEvent
    object UnselectNow : EpisodeTrackUiEvent
    object SelectFirstAired : EpisodeTrackUiEvent
    data class SelectDate(val date: LocalDate) : EpisodeTrackUiEvent
    data class SelectTime(val time: LocalTime) : EpisodeTrackUiEvent
    data class ClearMessage(val id: Long) : EpisodeTrackUiEvent
}
