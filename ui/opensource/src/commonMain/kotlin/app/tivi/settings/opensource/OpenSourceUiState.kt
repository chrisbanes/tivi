// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.opensource

import androidx.compose.runtime.Immutable
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class OpenSourceUiState(
    val hideArtwork: Boolean,
    val eventSink: (OpenSourceUiEvent) -> Unit,
) : CircuitUiState

sealed interface OpenSourceUiEvent : CircuitUiEvent {
    data object NavigateUp : OpenSourceUiEvent
    data object NavigateLog : OpenSourceUiEvent
    data object ToggleHideArtwork : OpenSourceUiEvent
}
