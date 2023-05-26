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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.Dp
import com.slack.circuit.foundation.CircuitContent
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.runtime.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetOverlay<Model : Any, Result : Any>(
    private val model: Model,
    private val onDismiss: () -> Result,
    private val tonalElevation: Dp = BottomSheetDefaults.Elevation,
    private val scrimColor: Color = Color.Unspecified,
    private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {
    @Composable
    override fun Content(navigator: OverlayNavigator<Result>) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        val coroutineScope = rememberCoroutineScope()
        BackHandler(enabled = sheetState.isVisible) {
            coroutineScope
                .launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        navigator.finish(onDismiss())
                    }
                }
        }

        ModalBottomSheet(
            modifier = Modifier.fillMaxWidth(),
            content = {
                // Delay setting the result until we've finished dismissing
                content(model) { result ->
                    // This is the OverlayNavigator.finish() callback
                    coroutineScope.launch {
                        try {
                            sheetState.hide()
                        } finally {
                            navigator.finish(result)
                        }
                    }
                }
            },
            tonalElevation = tonalElevation,
            scrimColor = if (scrimColor.isSpecified) scrimColor else BottomSheetDefaults.ScrimColor,
            sheetState = sheetState,
            onDismissRequest = { navigator.finish(onDismiss()) },
        )

        LaunchedEffect(Unit) { sheetState.show() }
    }
}

suspend fun OverlayHost.showInBottomSheet(
    screen: Screen,
): Unit = show(
    BottomSheetOverlay(Unit, {}) { _, _ ->
        CircuitContent(screen = screen)
    },
)
