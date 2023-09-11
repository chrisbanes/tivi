// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import androidx.compose.runtime.Immutable
import app.tivi.data.licenses.OpenSourceItem
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class OpenSourceUiState(
    val opensourceItemList: List<OpenSourceItem> = emptyList(),
    val eventSink: (OpenSourceUiEvent) -> Unit,
) : CircuitUiState

sealed interface OpenSourceUiEvent : CircuitUiEvent {
    data object NavigateUp : OpenSourceUiEvent
    data class NavigateRepository(val url: String) : OpenSourceUiEvent
}
