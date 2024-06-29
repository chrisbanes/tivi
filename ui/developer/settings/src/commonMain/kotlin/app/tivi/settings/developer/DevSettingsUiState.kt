// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class DevSettingsUiState(
  val hideArtwork: Boolean,
  val eventSink: (DevSettingsUiEvent) -> Unit,
) : CircuitUiState

sealed interface DevSettingsUiEvent : CircuitUiEvent {
  data object NavigateUp : DevSettingsUiEvent
  data object NavigateLog : DevSettingsUiEvent
  data object NavigateNotifications : DevSettingsUiEvent
  data object ToggleHideArtwork : DevSettingsUiEvent
}
