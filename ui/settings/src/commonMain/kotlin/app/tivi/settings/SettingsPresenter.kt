// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.common.compose.collectAsState
import app.tivi.core.permissions.Permission.REMOTE_NOTIFICATION
import app.tivi.core.permissions.PermissionState
import app.tivi.core.permissions.PermissionsController
import app.tivi.core.permissions.performPermissionedAction
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.domain.observers.ObserveTraktAuthState
import app.tivi.screens.DevSettingsScreen
import app.tivi.screens.LicensesScreen
import app.tivi.screens.SettingsScreen
import app.tivi.screens.UrlScreen
import app.tivi.util.launchOrThrow
import app.tivi.wrapEventSink
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.CoroutineScope
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
  observeTraktAuthState: Lazy<ObserveTraktAuthState>,
  private val applicationInfo: ApplicationInfo,
) : Presenter<SettingsUiState> {
  private val preferences by preferences
  private val permissionsController by permissionsController
  private val observeTraktAuthState by observeTraktAuthState

  @Composable
  override fun present(): SettingsUiState {
    val theme by preferences.theme.collectAsState()
    val useDynamicColors by preferences.useDynamicColors.collectAsState()
    val useLessData by preferences.useLessData.collectAsState()
    val ignoreSpecials by preferences.ignoreSpecials.collectAsState()
    val crashDataReportingEnabled by preferences.reportAppCrashes.collectAsState()
    val analyticsDataReportingEnabled by preferences.reportAnalytics.collectAsState()

    val authState by observeTraktAuthState.flow.collectAsState(TraktAuthState.LOGGED_OUT)

    val notificationsEnabled by preferences.episodeAiringNotificationsEnabled.collectAsState()

    LaunchedEffect(observeTraktAuthState) {
      observeTraktAuthState(Unit)
    }

    val eventSink: CoroutineScope.(SettingsUiEvent) -> Unit = { event ->
      when (event) {
        SettingsUiEvent.NavigateUp -> {
          navigator.pop()
        }

        is SettingsUiEvent.SetTheme -> {
          launchOrThrow { preferences.theme.set(event.theme) }
        }

        SettingsUiEvent.ToggleUseDynamicColors -> {
          launchOrThrow { preferences.useDynamicColors.toggle() }
        }

        SettingsUiEvent.ToggleUseLessData -> {
          launchOrThrow { preferences.useLessData.toggle() }
        }

        SettingsUiEvent.ToggleIgnoreSpecials -> {
          launchOrThrow { preferences.ignoreSpecials.toggle() }
        }

        SettingsUiEvent.ToggleCrashDataReporting -> {
          launchOrThrow { preferences.reportAppCrashes.toggle() }
        }

        SettingsUiEvent.ToggleAnalyticsDataReporting -> {
          launchOrThrow { preferences.reportAnalytics.toggle() }
        }

        SettingsUiEvent.ToggleAiringEpisodeNotificationsEnabled -> {
          launchOrThrow {
            if (preferences.episodeAiringNotificationsEnabled.get()) {
              // If we're enabled, and being turned off, we don't need to mess with permissions
              preferences.episodeAiringNotificationsEnabled.set(false)
            } else {
              // If we're disabled, and being turned on, we need to check our permissions
              permissionsController.performPermissionedAction(REMOTE_NOTIFICATION) { state ->
                if (state == PermissionState.Granted) {
                  preferences.episodeAiringNotificationsEnabled.set(true)
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

        SettingsUiEvent.DeleteAccount -> {
          navigator.goTo(UrlScreen("https://trakt.tv/settings"))
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
      airingEpisodeNotificationsEnabled = notificationsEnabled,
      applicationInfo = applicationInfo,
      showDeveloperSettings = applicationInfo.flavor == Flavor.Qa,
      isLoggedIn = authState == TraktAuthState.LOGGED_IN,
      showDeleteAccount = authState == TraktAuthState.LOGGED_IN,
      eventSink = wrapEventSink(eventSink),
    )
  }
}
