// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.tivi.inject.DesktopApplicationComponent
import app.tivi.inject.WindowComponent
import app.tivi.inject.create

fun main() = application {
    val applicationComponent = remember {
        DesktopApplicationComponent.create()
    }

    Window(
        title = "Tivi",
        onCloseRequest = ::exitApplication,
    ) {
        val component = remember(applicationComponent) {
            WindowComponent.create(applicationComponent)
        }

        component.tiviContent(
            onRootPop = {
                // TODO
            },
            onOpenSettings = {
                // TODO
            },
            modifier = Modifier,
        )
    }
}
