// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.opensource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.tivi.screens.DevLogScreen
import app.tivi.screens.OpenSourceScreen
import app.tivi.settings.TiviPreferences
import app.tivi.settings.toggle
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class OpenSourceUiPresenterFactory(
    private val presenterFactory: (Navigator) -> OpenSourcePresenter,
) : Presenter.Factory {
    override fun create(
        screen: Screen,
        navigator: Navigator,
        context: CircuitContext,
    ): Presenter<*>? = when (screen) {
        is OpenSourceScreen -> presenterFactory(navigator)
        else -> null
    }
}

@Inject
class OpenSourcePresenter(
    @Assisted private val navigator: Navigator,
    private val preferences: TiviPreferences,
) : Presenter<OpenSourceUiState> {

    @Composable
    override fun present(): OpenSourceUiState {
        val hideArtwork by preferences.observeDeveloperHideArtwork().collectAsState(false)

        fun eventSink(event: OpenSourceUiEvent) {
            when (event) {
                OpenSourceUiEvent.NavigateUp -> navigator.pop()
                OpenSourceUiEvent.NavigateLog -> navigator.goTo(DevLogScreen)
                OpenSourceUiEvent.ToggleHideArtwork -> preferences::developerHideArtwork.toggle()
            }
        }

        return OpenSourceUiState(
            hideArtwork = hideArtwork,
            eventSink = ::eventSink,
        )
    }
}
