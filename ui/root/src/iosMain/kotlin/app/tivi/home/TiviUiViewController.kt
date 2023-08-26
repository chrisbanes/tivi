// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.circuit.push
import app.tivi.circuit.rememberCircuitNavigator
import app.tivi.circuit.rememberTiviBackStack
import app.tivi.screens.DiscoverScreen
import me.tatarka.inject.annotations.Inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

typealias TiviUiViewController = () -> UIViewController

@Inject
fun TiviUiViewController(
    tiviContent: TiviContent,
): UIViewController = ComposeUIViewController {
    val backstack = rememberTiviBackStack { push(DiscoverScreen) }
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
