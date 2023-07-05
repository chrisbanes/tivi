// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import app.tivi.screens.DiscoverScreen
import com.slack.circuit.backstack.rememberSaveableBackStack
import com.slack.circuit.foundation.push
import com.slack.circuit.foundation.rememberCircuitNavigator
import me.tatarka.inject.annotations.Inject
import platform.UIKit.UIViewController

typealias TiviUiViewController = () -> UIViewController

@Inject
fun TiviUiViewController(
    tiviContent: TiviContent,
): UIViewController = ComposeUIViewController {
    val backstack = rememberSaveableBackStack { push(DiscoverScreen) }
    val navigator = rememberCircuitNavigator(backstack, onRootPop = { /* no-op */ })

    tiviContent(
        backstack = backstack,
        navigator = navigator,
        onOpenSettings = { /* no-op */ },
        modifier = Modifier,
    )
}
