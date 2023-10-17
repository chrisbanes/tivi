// Copyright 2018, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episode.track

import androidx.compose.runtime.Immutable
import app.tivi.common.compose.UiMessage
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Immutable
data class EpisodeTrackUiState(
  val season: Season? = null,
  val episode: Episode? = null,
  val showEpisodeInfo: Boolean = true,
  val refreshing: Boolean = false,
  val message: UiMessage? = null,

  val canSubmit: Boolean = false,
  val submitInProgress: Boolean = false,

  val showSetFirstAired: Boolean = false,
  val selectedDate: LocalDate,
  val selectedTime: LocalTime,

  val eventSink: (EpisodeTrackUiEvent) -> Unit,
) : CircuitUiState

sealed interface EpisodeTrackUiEvent : CircuitUiEvent {
  data class Refresh(val fromUser: Boolean = false) : EpisodeTrackUiEvent
  object Submit : EpisodeTrackUiEvent
  object SelectNow : EpisodeTrackUiEvent
  object SelectFirstAired : EpisodeTrackUiEvent
  data class SelectDate(val date: LocalDate) : EpisodeTrackUiEvent
  data class SelectTime(val time: LocalTime) : EpisodeTrackUiEvent
  data class ClearMessage(val id: Long) : EpisodeTrackUiEvent
}
