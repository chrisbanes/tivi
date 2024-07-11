// Copyright (C) 2022 Slack Technologies, LLC
// SPDX-License-Identifier: Apache-2.0

package app.tivi.overlays

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
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.foundation.internal.BackHandler
import com.slack.circuit.foundation.onNavEvent
import com.slack.circuit.overlay.Overlay
import com.slack.circuit.overlay.OverlayHost
import com.slack.circuit.overlay.OverlayNavigator
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class BottomSheetOverlay<Model : Any, Result : Any>(
  private val model: Model,
  private val onDismiss: () -> Result,
  private val tonalElevation: Dp = BottomSheetDefaults.Elevation,
  private val scrimColor: Color = Color.Unspecified,
  private val skipPartiallyExpanded: Boolean = false,
  private val dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
  private val content: @Composable (Model, OverlayNavigator<Result>) -> Unit,
) : Overlay<Result> {
  @Composable
  override fun Content(navigator: OverlayNavigator<Result>) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded)

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
      dragHandle = dragHandle,
      tonalElevation = tonalElevation,
      scrimColor = if (scrimColor.isSpecified) scrimColor else BottomSheetDefaults.ScrimColor,
      sheetState = sheetState,
      onDismissRequest = { navigator.finish(onDismiss()) },
    )

    LaunchedEffect(Unit) { sheetState.show() }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
suspend fun OverlayHost.showInBottomSheet(
  screen: Screen,
  tonalElevation: Dp = BottomSheetDefaults.Elevation,
  scrimColor: Color = Color.Unspecified,
  skipPartiallyExpanded: Boolean = false,
  hostNavigator: Navigator? = null,
  dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
): Unit = show(
  BottomSheetOverlay(
    model = Unit,
    tonalElevation = tonalElevation,
    scrimColor = scrimColor,
    dragHandle = dragHandle,
    skipPartiallyExpanded = skipPartiallyExpanded,
    onDismiss = {},
  ) { _, navigator ->
    CircuitContent(
      screen = screen,
      onNavEvent = { event ->
        when (event) {
          is NavEvent.Pop -> navigator.finish(Unit)
          else -> hostNavigator?.onNavEvent(event)
        }
      },
    )
  },
)
