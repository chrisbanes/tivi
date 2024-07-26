// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import app.tivi.app.ApplicationInfo
import app.tivi.app.Flavor
import app.tivi.common.compose.collectAsState
import app.tivi.core.permissions.Permission.REMOTE_NOTIFICATION
import app.tivi.core.permissions.PermissionState
import app.tivi.core.permissions.PermissionsController
import app.tivi.core.permissions.performPermissionedAction
import app.tivi.entitlements.EntitlementManager
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
  entitlements: Lazy<EntitlementManager>,
  private val applicationInfo: ApplicationInfo,
) : Presenter<SettingsUiState> {
  private val preferences by preferences
  private val permissionsController by permissionsController
  private val entitlements by entitlements

  @Composable
  override fun present(): SettingsUiState {
    val theme by preferences.theme.collectAsState()
    val useDynamicColors by preferences.useDynamicColors.collectAsState()
    val useLessData by preferences.useLessData.collectAsState()
    val ignoreSpecials by preferences.ignoreSpecials.collectAsState()
    val crashDataReportingEnabled by preferences.reportAppCrashes.collectAsState()
    val analyticsDataReportingEnabled by preferences.reportAnalytics.collectAsState()

    val isPro by produceState(initialValue = false, entitlements) {
      entitlements.observeProEntitlement().collect { value = it }
    }
    val notificationsEnabled by preferences.episodeAiringNotificationsEnabled.collectAsState()
    var proUpsellVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun eventSink(event: SettingsUiEvent) {
      when (event) {
        SettingsUiEvent.NavigateUp -> {
          navigator.pop()
        }

        is SettingsUiEvent.SetTheme -> {
          coroutineScope.launch { preferences.theme.set(event.theme) }
        }

        SettingsUiEvent.ToggleUseDynamicColors -> {
          coroutineScope.launch { preferences.useDynamicColors.toggle() }
        }

        SettingsUiEvent.ToggleUseLessData -> {
          coroutineScope.launch { preferences.useLessData.toggle() }
        }

        SettingsUiEvent.ToggleIgnoreSpecials -> {
          coroutineScope.launch { preferences.ignoreSpecials.toggle() }
        }

        SettingsUiEvent.ToggleCrashDataReporting -> {
          coroutineScope.launch { preferences.reportAppCrashes.toggle() }
        }

        SettingsUiEvent.ToggleAnalyticsDataReporting -> {
          coroutineScope.launch { preferences.reportAnalytics.toggle() }
        }

        SettingsUiEvent.ToggleAiringEpisodeNotificationsEnabled -> {
          if (isPro) {
            coroutineScope.launch {
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
          } else {
            proUpsellVisible = true
          }
        }

        SettingsUiEvent.NavigatePrivacyPolicy -> {
          navigator.goTo(UrlScreen("https://chrisbanes.github.io/tivi/privacypolicy"))
        }

        SettingsUiEvent.NavigateOpenSource -> navigator.goTo(LicensesScreen)
        SettingsUiEvent.NavigateDeveloperSettings -> navigator.goTo(DevSettingsScreen)

        SettingsUiEvent.DismissProUpsell -> proUpsellVisible = false
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
      airingEpisodeNotificationsEnabled = notificationsEnabled && isPro,
      applicationInfo = applicationInfo,
      showDeveloperSettings = applicationInfo.flavor == Flavor.Qa,
      proUpsellVisible = proUpsellVisible,
      isPro = isPro,
      eventSink = ::eventSink,
    )
  }
}
