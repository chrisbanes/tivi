// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import androidx.compose.runtime.Immutable
import app.tivi.data.licenses.LicenseItem
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class LicensesUiState(
  val licenses: List<LicenseGroup> = emptyList(),
  val eventSink: (LicensesUiEvent) -> Unit,
) : CircuitUiState

sealed interface LicensesUiEvent : CircuitUiEvent {
  data object NavigateUp : LicensesUiEvent
  data class NavigateRepository(val artifact: LicenseItem) : LicensesUiEvent
}

data class LicenseGroup(
  val id: String,
  val artifacts: List<LicenseItem>,
)
