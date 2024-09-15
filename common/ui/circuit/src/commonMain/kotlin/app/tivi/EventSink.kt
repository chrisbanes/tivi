// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import co.touchlab.kermit.Logger
import com.slack.circuit.runtime.CircuitUiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

@Composable
inline fun <E : CircuitUiEvent> wrapEventSink(
  crossinline eventSink: CoroutineScope.(E) -> Unit,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
): (E) -> Unit = { event ->
  if (coroutineScope.isActive) {
    coroutineScope.eventSink(event)
  } else {
    Logger.i(IllegalStateException()) {
      "Received event, but CoroutineScope is no longer active. See stack trace for caller."
    }
  }
}
