// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.notifications

import androidx.compose.runtime.Immutable
import app.tivi.data.models.Notification
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class DevNotificationsUiState(
  val pendingNotifications: List<Notification>,
  val eventSink: (DevNotificationsUiEvent) -> Unit,
) : CircuitUiState

sealed interface DevNotificationsUiEvent : CircuitUiEvent {
  data object NavigateUp : DevNotificationsUiEvent
  data object ShowEpisodeAiringNotification : DevNotificationsUiEvent
  data object ShowNotification : DevNotificationsUiEvent
  data object ScheduleNotification : DevNotificationsUiEvent
}
