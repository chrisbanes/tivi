// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import app.tivi.BuildConfig
import app.tivi.TiviActivity
import app.tivi.TiviApplication
import app.tivi.inject.AndroidActivityComponent
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.inject.create
import app.tivi.screens.DiscoverScreen
import app.tivi.settings.TiviPreferences
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator

class MainActivity : TiviActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()

    super.onCreate(savedInstanceState)
    val component = AndroidActivityComponent.create(this, AndroidApplicationComponent.from(this))

    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      val backstack = rememberSaveableBackStack { push(DiscoverScreen) }
      val navigator = rememberCircuitNavigator(backstack)

      val isSystemInDarkTheme = isSystemInDarkTheme()
      val theme by component.applicationComponent.preferences.observeTheme()
        .collectAsState(TiviPreferences.Theme.SYSTEM)
      val isDarkTheme by remember {
        derivedStateOf {
          when (theme) {
            TiviPreferences.Theme.SYSTEM -> isSystemInDarkTheme
            TiviPreferences.Theme.LIGHT -> false
            TiviPreferences.Theme.DARK -> true
          }
        }
      }
      LaunchedEffect(isDarkTheme) {
        enableEdgeToEdge(
          statusBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
            detectDarkMode = { isDarkTheme },
          ),
          navigationBarStyle = SystemBarStyle.auto(
            lightScrim = Color.TRANSPARENT,
            darkScrim = Color.TRANSPARENT,
            detectDarkMode = { isDarkTheme },
          ),
        )
      }

      component.tiviContent(
        backstack,
        navigator,
        { url ->
          val intent = CustomTabsIntent.Builder().build()
          intent.launchUrl(this@MainActivity, Uri.parse(url))
        },
        Modifier.semantics {
          // Enables testTag -> UiAutomator resource id
          // See https://developer.android.com/jetpack/compose/testing#uiautomator-interop
          @OptIn(ExperimentalComposeUiApi::class)
          testTagsAsResourceId = BuildConfig.DEBUG
        },
      )
    }
  }
}

private fun AndroidApplicationComponent.Companion.from(context: Context): AndroidApplicationComponent {
  return (context.applicationContext as TiviApplication).component
}
