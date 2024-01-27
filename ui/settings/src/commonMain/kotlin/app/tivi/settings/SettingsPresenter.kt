// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.screens.DevSettingsScreen
import app.tivi.screens.LicensesScreen
import app.tivi.screens.SettingsScreen
import app.tivi.screens.UrlScreen
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class SettingsUiPresenterFactory(
  private val presenterFactory: (Navigator) -> SettingsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is SettingsScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class SettingsPresenter(
  @Assisted private val navigator: Navigator,
  preferences: Lazy<TiviPreferences>,
  private val applicationInfo: ApplicationInfo,
) : Presenter<SettingsUiState> {
  private val preferences by preferences

  @Composable
  override fun present(): SettingsUiState {
    val theme by remember { preferences.observeTheme() }
      .collectAsRetainedState(TiviPreferences.Theme.SYSTEM)

    val useDynamicColors by remember { preferences.observeUseDynamicColors() }
      .collectAsRetainedState(false)

    val useLessData by remember { preferences.observeUseLessData() }
      .collectAsRetainedState(false)

    val ignoreSpecials by remember { preferences.observeIgnoreSpecials() }
      .collectAsRetainedState(true)

    val crashDataReportingEnabled by remember { preferences.observeReportAppCrashes() }
      .collectAsRetainedState(true)

    val analyticsDataReportingEnabled by remember { preferences.observeReportAnalytics() }
      .collectAsRetainedState(true)

    fun eventSink(event: SettingsUiEvent) {
      when (event) {
        SettingsUiEvent.NavigateUp -> navigator.pop()
        is SettingsUiEvent.SetTheme -> {
          preferences.theme = event.theme
        }

        SettingsUiEvent.ToggleUseDynamicColors -> preferences::useDynamicColors.toggle()
        SettingsUiEvent.ToggleUseLessData -> preferences::useLessData.toggle()
        SettingsUiEvent.ToggleIgnoreSpecials -> preferences::ignoreSpecials.toggle()
        SettingsUiEvent.ToggleCrashDataReporting -> preferences::reportAppCrashes.toggle()
        SettingsUiEvent.ToggleAnalyticsDataReporting -> preferences::reportAnalytics.toggle()
        SettingsUiEvent.NavigatePrivacyPolicy -> {
          navigator.goTo(UrlScreen("https://chrisbanes.github.io/tivi/privacypolicy"))
        }

        SettingsUiEvent.NavigateOpenSource -> {
          navigator.goTo(LicensesScreen)
        }

        SettingsUiEvent.NavigateDeveloperSettings -> {
          navigator.goTo(DevSettingsScreen)
        }
      }
    }

    return SettingsUiState(
      theme = theme,
      useDynamicColors = useDynamicColors,
      dynamicColorsAvailable = DynamicColorsAvailable,
      openSourceLicenseAvailable = OpenSourceLicenseAvailable,
      useLessData = useLessData,
      ignoreSpecials = ignoreSpecials,
      crashDataReportingEnabled = crashDataReportingEnabled,
      analyticsDataReportingEnabled = analyticsDataReportingEnabled,
      applicationInfo = applicationInfo,
      showDeveloperSettings = applicationInfo.flavor == Flavor.Qa,
      eventSink = ::eventSink,
    )
  }
}
