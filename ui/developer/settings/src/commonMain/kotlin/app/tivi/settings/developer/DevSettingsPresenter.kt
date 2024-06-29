// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.tivi.common.compose.collectAsState
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.core.notifications.NotificationChannel
import app.tivi.core.notifications.NotificationManager
import app.tivi.screens.DevLogScreen
import app.tivi.screens.DevSettingsScreen
import app.tivi.settings.TiviPreferences
import app.tivi.settings.toggle
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class DevSettingsUiPresenterFactory(
  private val presenterFactory: (Navigator) -> DevSettingsPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is DevSettingsScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class DevSettingsPresenter(
  @Assisted private val navigator: Navigator,
  private val preferences: Lazy<TiviPreferences>,
  private val notification: NotificationManager,
) : Presenter<DevSettingsUiState> {

  @Composable
  override fun present(): DevSettingsUiState {
    val hideArtwork by preferences.value.developerHideArtwork.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    fun eventSink(event: DevSettingsUiEvent) {
      when (event) {
        DevSettingsUiEvent.NavigateUp -> navigator.pop()
        DevSettingsUiEvent.NavigateLog -> navigator.goTo(DevLogScreen)
        DevSettingsUiEvent.ToggleHideArtwork -> {
          coroutineScope.launch { preferences.value.developerHideArtwork.toggle() }
        }
        DevSettingsUiEvent.ScheduleNotification -> {
          notification.schedule(
            id = "scheduled_test",
            title = "Test Notification",
            message = "Scheduled from developer settings",
            channel = NotificationChannel.DEVELOPER,
            date = Clock.System.now() + 15.minutes,
          )
        }
        DevSettingsUiEvent.ShowNotification -> {
          notification.schedule(
            id = "immediate_test",
            title = "Test Notification",
            message = "Sent from developer settings",
            channel = NotificationChannel.DEVELOPER,
            date = Clock.System.now() + 5.seconds,
          )
        }
      }
    }

    return DevSettingsUiState(
      hideArtwork = hideArtwork,
      eventSink = ::eventSink,
    )
  }
}
