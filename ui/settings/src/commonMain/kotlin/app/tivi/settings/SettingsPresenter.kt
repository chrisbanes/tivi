// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.app.ApplicationInfo
import app.tivi.screens.SettingsScreen
import app.tivi.screens.UrlScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.presenter.Presenter
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
    private val preferences: TiviPreferences,
    private val applicationInfo: ApplicationInfo,
) : Presenter<SettingsUiState> {

    @Composable
    override fun present(): SettingsUiState {
        val theme by remember { preferences.observeTheme() }
            .collectAsState(TiviPreferences.Theme.SYSTEM)

        val useDynamicColors by remember { preferences.observeUseDynamicColors() }
            .collectAsState(false)

        val useLessData by remember { preferences.observeUseLessData() }
            .collectAsState(false)

        fun eventSink(event: SettingsUiEvent) {
            when (event) {
                SettingsUiEvent.NavigateUp -> navigator.pop()
                is SettingsUiEvent.SetTheme -> {
                    preferences.theme = event.theme
                }
                SettingsUiEvent.ToggleUseDynamicColors -> {
                    preferences.useDynamicColors = !preferences.useDynamicColors
                }
                SettingsUiEvent.ToggleUseLessData -> {
                    preferences.useLessData = !preferences.useLessData
                }
                SettingsUiEvent.NavigatePrivacyPolicy -> {
                    navigator.goTo(UrlScreen("https://chrisbanes.github.io/tivi/privacypolicy"))
                }
            }
        }

        return SettingsUiState(
            theme = theme,
            useDynamicColors = useDynamicColors,
            dynamicColorsAvailable = DynamicColorsAvailable,
            useLessData = useLessData,
            applicationInfo = applicationInfo,
            eventSink = ::eventSink,
        )
    }
}
