// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.screens.DiscoverScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.rememberCircuitNavigator
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

typealias TiviUiViewController = () -> UIViewController

@Inject
@Suppress("ktlint:standard:function-naming")
fun TiviUiViewController(
  tiviContent: TiviContent,
): UIViewController = ComposeUIViewController {
  val backstack = rememberSaveableBackStack { push(DiscoverScreen) }
  val navigator = rememberCircuitNavigator(backstack, onRootPop = { /* no-op */ })
  val uiViewController = LocalUIViewController.current

  tiviContent(
    backstack,
    navigator,
    { url ->
      val safari = SFSafariViewController(NSURL(string = url))
      uiViewController.presentViewController(safari, animated = true, completion = null)
    },
    Modifier,
  )
}
