// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.core.permissions.Permission.REMOTE_NOTIFICATION
import app.tivi.core.permissions.PermissionState
import app.tivi.core.permissions.PermissionsController
import app.tivi.core.permissions.performPermissionedAction
import app.tivi.screens.DevSettingsScreen
import app.tivi.screens.LicensesScreen
import app.tivi.screens.SettingsScreen
import app.tivi.screens.UrlScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
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
  permissionsController: Lazy<PermissionsController>,
  private val applicationInfo: ApplicationInfo,
) : Presenter<SettingsUiState> {
  private val preferences by preferences
  private val permissionsController by permissionsController

  @Composable
  override fun present(): SettingsUiState {
    val theme by remember { preferences.observeTheme() }
      .collectAsState(TiviPreferences.Theme.SYSTEM)

    val useDynamicColors by remember { preferences.observeUseDynamicColors() }
      .collectAsState(false)

    val useLessData by remember { preferences.observeUseLessData() }
      .collectAsState(false)

    val ignoreSpecials by remember { preferences.observeIgnoreSpecials() }
      .collectAsState(true)

    val crashDataReportingEnabled by remember { preferences.observeReportAppCrashes() }
      .collectAsState(true)

    val analyticsDataReportingEnabled by remember { preferences.observeReportAnalytics() }
      .collectAsState(true)

    val notificationsEnabled by preferences.notificationsEnabled.flow.collectAsState(false)

    val coroutineScope = rememberCoroutineScope()

    fun eventSink(event: SettingsUiEvent) {
      when (event) {
        SettingsUiEvent.NavigateUp -> {
          navigator.pop()
        }
        is SettingsUiEvent.SetTheme -> {
          coroutineScope.launch { preferences.setTheme(event.theme) }
        }

        SettingsUiEvent.ToggleUseDynamicColors -> {
          coroutineScope.launch { preferences.toggleUseDynamicColors() }
        }
        SettingsUiEvent.ToggleUseLessData -> {
          coroutineScope.launch { preferences.toggleUseLessData() }
        }
        SettingsUiEvent.ToggleIgnoreSpecials -> {
          coroutineScope.launch { preferences.toggleIgnoreSpecials() }
        }
        SettingsUiEvent.ToggleCrashDataReporting -> {
          coroutineScope.launch { preferences.toggleReportAppCrashes() }
        }
        SettingsUiEvent.ToggleAnalyticsDataReporting -> {
          coroutineScope.launch { preferences.toggleReportAnalytics() }
        }
        SettingsUiEvent.ToggleAiringEpisodeNotificationsEnabled -> {
          coroutineScope.launch {
            if (preferences.notificationsEnabled.get()) {
              // If we're enabled, and being turned off, we don't need to mess with permissions
              preferences.notificationsEnabled.toggle()
            } else {
              // If we're disabled, and being turned on, we need to check our permissions
              permissionsController.performPermissionedAction(REMOTE_NOTIFICATION) { state ->
                if (state == PermissionState.Granted) {
                  preferences.notificationsEnabled.toggle()
                } else {
                  permissionsController.openAppSettings()
                }
              }
            }
          }
        }
        SettingsUiEvent.NavigatePrivacyPolicy -> {
          navigator.goTo(UrlScreen("https://chrisbanes.github.io/tivi/privacypolicy"))
        }

        SettingsUiEvent.NavigateOpenSource -> navigator.goTo(LicensesScreen)
        SettingsUiEvent.NavigateDeveloperSettings -> navigator.goTo(DevSettingsScreen)
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
      airingEpisodeNotificationsEnabled = notificationsEnabled,
      applicationInfo = applicationInfo,
      showDeveloperSettings = applicationInfo.flavor == Flavor.Qa,
      eventSink = ::eventSink,
    )
  }
}
