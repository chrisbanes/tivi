// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Immutable
import app.tivi.app.ApplicationInfo
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState

@Immutable
data class SettingsUiState(
    val theme: TiviPreferences.Theme,
    val dynamicColorsAvailable: Boolean,
    val useDynamicColors: Boolean,
    val useLessData: Boolean,
    val ignoreSpecials: Boolean,
    val applicationInfo: ApplicationInfo,
    val eventSink: (SettingsUiEvent) -> Unit,
) : CircuitUiState

sealed interface SettingsUiEvent : CircuitUiEvent {
    object NavigateUp : SettingsUiEvent
    object NavigatePrivacyPolicy : SettingsUiEvent
    object ToggleUseDynamicColors : SettingsUiEvent
    object ToggleUseLessData : SettingsUiEvent
    object ToggleIgnoreSpecials : SettingsUiEvent
    data class SetTheme(val theme: TiviPreferences.Theme) : SettingsUiEvent
}
