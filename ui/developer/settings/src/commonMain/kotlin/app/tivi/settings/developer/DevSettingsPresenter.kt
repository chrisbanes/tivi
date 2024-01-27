// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.screens.DevLogScreen
import app.tivi.screens.DevSettingsScreen
import app.tivi.settings.TiviPreferences
import com.slack.circuit.retained.collectAsRetainedState
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch
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
) : Presenter<DevSettingsUiState> {

  @Composable
  override fun present(): DevSettingsUiState {
    val hideArtwork by remember { preferences.value.observeDeveloperHideArtwork() }
      .collectAsRetainedState(false)

    val coroutineScope = rememberCoroutineScope()

    fun eventSink(event: DevSettingsUiEvent) {
      when (event) {
        DevSettingsUiEvent.NavigateUp -> navigator.pop()
        DevSettingsUiEvent.NavigateLog -> navigator.goTo(DevLogScreen)
        DevSettingsUiEvent.ToggleHideArtwork -> {
          coroutineScope.launch { preferences.value.toggleDeveloperHideArtwork() }
        }
      }
    }

    return DevSettingsUiState(
      hideArtwork = hideArtwork,
      eventSink = ::eventSink,
    )
  }
}
