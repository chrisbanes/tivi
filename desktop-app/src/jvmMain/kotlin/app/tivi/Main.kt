// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.tivi.inject.DesktopApplicationComponent
import app.tivi.inject.WindowComponent
import app.tivi.inject.create
import app.tivi.screens.DiscoverScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator

fun main() = application {
  val applicationComponent = remember {
    DesktopApplicationComponent.create()
  }

  LaunchedEffect(applicationComponent) {
    applicationComponent.initializers.initialize()
  }

  Window(
    title = "Tivi",
    onCloseRequest = ::exitApplication,
  ) {
    val component = remember(applicationComponent) {
      WindowComponent.create(applicationComponent)
    }

    val backstack = rememberSaveableBackStack(listOf(DiscoverScreen))
    val navigator = rememberCircuitNavigator(backstack) { /* no-op */ }

    component.tiviContent.Content(
      backstack = backstack,
      navigator = navigator,
      onOpenUrl = {
        // no-op for now
        false
      },
      modifier = Modifier,
    )
  }
}
