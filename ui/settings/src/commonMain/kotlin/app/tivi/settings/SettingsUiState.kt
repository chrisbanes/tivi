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
  val openSourceLicenseAvailable: Boolean,
  val useDynamicColors: Boolean,
  val useLessData: Boolean,
  val ignoreSpecials: Boolean,
  val crashDataReportingEnabled: Boolean,
  val analyticsDataReportingEnabled: Boolean,
  val airingEpisodeNotificationsEnabled: Boolean,
  val applicationInfo: ApplicationInfo,
  val showDeveloperSettings: Boolean,
  val proUpsellVisible: Boolean,
  val isPro: Boolean,
  val eventSink: (SettingsUiEvent) -> Unit,
) : CircuitUiState

sealed interface SettingsUiEvent : CircuitUiEvent {
  data object NavigateUp : SettingsUiEvent
  data object NavigatePrivacyPolicy : SettingsUiEvent
  data object NavigateOpenSource : SettingsUiEvent
  data object NavigateDeveloperSettings : SettingsUiEvent
  data object ToggleUseDynamicColors : SettingsUiEvent
  data object ToggleUseLessData : SettingsUiEvent
  data object ToggleIgnoreSpecials : SettingsUiEvent
  data object ToggleCrashDataReporting : SettingsUiEvent
  data object ToggleAnalyticsDataReporting : SettingsUiEvent
  data object ToggleAiringEpisodeNotificationsEnabled : SettingsUiEvent
  data object DismissProUpsell : SettingsUiEvent
  data class SetTheme(val theme: TiviPreferences.Theme) : SettingsUiEvent
}
