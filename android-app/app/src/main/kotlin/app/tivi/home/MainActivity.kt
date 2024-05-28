// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import app.tivi.TiviActivity
import app.tivi.TiviApplication
import app.tivi.inject.AndroidActivityComponent
import app.tivi.inject.AndroidApplicationComponent
import app.tivi.inject.create
import app.tivi.screens.DiscoverScreen
import app.tivi.settings.TiviPreferences
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : TiviActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    enableEdgeToEdgeForTheme(TiviPreferences.Theme.SYSTEM)

    super.onCreate(savedInstanceState)

    val applicationComponent = AndroidApplicationComponent.from(this)
    val component = AndroidActivityComponent.create(this, applicationComponent)

    lifecycle.coroutineScope.launch {
      repeatOnLifecycle(Lifecycle.State.STARTED) {
        val prefs = withContext(applicationComponent.dispatchers.io) {
          applicationComponent.preferences.observeTheme()
        }
        prefs.collect(::enableEdgeToEdgeForTheme)
      }
    }

    setContent {
      val backstack = rememberSaveableBackStack(listOf(DiscoverScreen))
      val navigator = rememberCircuitNavigator(backstack)

      component.tiviContent.Content(
        backstack = backstack,
        navigator = navigator,
        onOpenUrl = { url ->
          val intent = CustomTabsIntent.Builder().build()
          intent.launchUrl(this@MainActivity, Uri.parse(url))
          true
        },
        modifier = Modifier.semantics {
          // Enables testTag -> UiAutomator resource id
          @OptIn(ExperimentalComposeUiApi::class)
          testTagsAsResourceId = true
        },
      )
    }
  }
}

private fun ComponentActivity.enableEdgeToEdgeForTheme(theme: TiviPreferences.Theme) {
  val style = when (theme) {
    TiviPreferences.Theme.LIGHT -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    TiviPreferences.Theme.DARK -> SystemBarStyle.dark(Color.TRANSPARENT)
    TiviPreferences.Theme.SYSTEM -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
  }
  enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
}

private fun AndroidApplicationComponent.Companion.from(context: Context): AndroidApplicationComponent {
  return (context.applicationContext as TiviApplication).component
}
