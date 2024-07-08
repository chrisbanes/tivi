// Copyright 2018, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.episodedetails

import androidx.compose.runtime.Immutable
import app.tivi.common.compose.UiMessage
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
  data class Refresh(val fromUser: Boolean) : EpisodeDetailsUiEvent
  data class RemoveWatchEntry(val id: Long) : EpisodeDetailsUiEvent
  data class ClearMessage(val id: Long) : EpisodeDetailsUiEvent
  data object RemoveAllWatches : EpisodeDetailsUiEvent
  data object OpenTrackEpisode : EpisodeDetailsUiEvent
  data object ExpandToShowDetails : EpisodeDetailsUiEvent
  data object NavigateUp : EpisodeDetailsUiEvent
}
