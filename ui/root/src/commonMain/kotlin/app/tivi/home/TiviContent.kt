// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.compose.shouldUseDarkColors
import app.tivi.common.compose.shouldUseDynamicColors
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.core.analytics.Analytics
import app.tivi.overlays.LocalNavigator
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.SettingsScreen
import app.tivi.screens.TiviScreen
import app.tivi.settings.TiviPreferences
import com.slack.circuit.backstack.SaveableBackStack
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import com.slack.circuit.foundation.screen
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.Screen

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun TiviContent(
    onRootPop: () -> Unit,
    onOpenSettings: () -> Unit,
    analytics: Analytics,
    preferences: TiviPreferences,
    modifier: Modifier = Modifier,
) {
    val backstack: SaveableBackStack = rememberSaveableBackStack { push(DiscoverScreen) }
    val circuitNavigator = rememberCircuitNavigator(backstack, onRootPop)

    val navigator: Navigator = remember(circuitNavigator) {
        TiviNavigator(circuitNavigator, onOpenSettings)
    }

    // Launch an effect to track changes to the current back stack entry, and push them
    // as a screen views to analytics
    LaunchedEffect(backstack.topRecord) {
        val topScreen = backstack.topRecord?.screen as? TiviScreen
        analytics.trackScreenView(
            name = topScreen?.name ?: "unknown screen",
            arguments = topScreen?.arguments,
        )
    }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalWindowSizeClass provides calculateWindowSizeClass(),
    ) {
        TiviTheme(
            useDarkColors = preferences.shouldUseDarkColors(),
            useDynamicColors = preferences.shouldUseDynamicColors(),
        ) {
            Home(
                backstack = backstack,
                navigator = navigator,
                modifier = modifier,
            )
        }
    }
}

private class TiviNavigator(
    private val navigator: Navigator,
    private val onOpenSettings: () -> Unit,
) : Navigator {
    override fun goTo(screen: Screen) {
        when (screen) {
            is SettingsScreen -> onOpenSettings()
            else -> navigator.goTo(screen)
        }
    }

    override fun pop(): Screen? {
        return navigator.pop()
    }

    override fun resetRoot(newRoot: Screen): List<Screen> {
        return navigator.resetRoot(newRoot)
    }
}
