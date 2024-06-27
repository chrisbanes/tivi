// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.CircuitUiEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

@Composable
fun <E : CircuitUiEvent> rememberEventChannel(): Channel<E> = remember {
  object : RememberObserver {
    val channel = Channel<E>(Channel.BUFFERED)

    override fun onRemembered() = Unit

    override fun onAbandoned() {
      channel.close()
    }

    override fun onForgotten() {
      channel.close()
    }
  }.channel
}

@Composable
fun <E : CircuitUiEvent> LaunchedEventProcessor(
  channel: Channel<E>,
  processors: Int = 3,
  block: suspend (E) -> Unit,
) {
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
