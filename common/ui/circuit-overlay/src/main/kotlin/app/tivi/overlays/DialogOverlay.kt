/*
 * Copyright 2022 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
