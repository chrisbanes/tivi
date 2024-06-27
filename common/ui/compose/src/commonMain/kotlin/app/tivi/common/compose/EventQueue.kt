// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.CircuitUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
fun <E: CircuitUiEvent> rememberEventChannel(): Channel<E> = remember { Channel(Channel.BUFFERED) }

@Composable
fun <E: CircuitUiEvent> LaunchedEventProcessor(channel: Channel<E>, processors: Int = 3, block: (E) -> Unit) {
  LaunchedEffect(channel, processors) {
    repeat(processors) {
      launch {
        for (event in channel) {
          block(event)
        }
      }
    }
  }
}
