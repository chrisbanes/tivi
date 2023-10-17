// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.log

import androidx.compose.runtime.Immutable
import app.tivi.util.LogMessage
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class DevLogUiState(
  val logs: List<LogMessage>,
  val eventSink: (DevLogUiEvent) -> Unit,
) : CircuitUiState

sealed interface DevLogUiEvent : CircuitUiEvent {
  data object NavigateUp : DevLogUiEvent
}
