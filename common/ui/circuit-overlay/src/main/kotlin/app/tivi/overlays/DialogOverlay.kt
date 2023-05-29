// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.overlays

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Dialog
import app.tivi.common.compose.rememberCoroutineScope
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.runtime.Screen
import kotlinx.coroutines.launch

class DialogOverlay<Model : Any, Result : Any>(
    private val model: Model,
    private val onDismiss: () -> Result,
    private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        val coroutineScope = rememberCoroutineScope()
        Dialog(
            onDismissRequest = { navigator.finish(onDismiss()) },
        ) {
            // Delay setting the result until we've finished dismissing
            content(model) { result ->
                // This is the OverlayNavigator.finish() callback
                coroutineScope.launch {
                    navigator.finish(result)
                }
            }
        }
    }
}

suspend fun OverlayHost.showInDialog(
    screen: Screen,
): Unit = show(
    DialogOverlay(Unit, {}) { _, _ ->
        CircuitContent(screen = screen)
    },
)
