// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.remember
import com.slack.circuit.runtime.CircuitUiEvent
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/**
 * Marker interface for events which should be processed 'immediately'.
 *
 * Primarily, this is meant for events which are driven by user input and trigger UI changes
 * (click button to go back), etc.
 */
interface ImmediateExecutionEvent : CircuitUiEvent

class EventQueue<E : CircuitUiEvent> {
  private val immediateQueue = Channel<E>(Channel.BUFFERED)
  private val longRunningQueue = Channel<E>(Channel.BUFFERED)

  fun send(event: E) {
    if (event is ImmediateExecutionEvent) {
      immediateQueue.trySend(event)
    } else {
      longRunningQueue.trySend(event)
    }
  }

  suspend fun run(processors: Int, block: suspend (E) -> Unit) = coroutineScope {
    launch {
      // Our first processor processes the `immediateQueue`. Each event has a very small
      // amount of time to process their event. This is meant for events which are driven by
      // user input (click button to go back), etc
      for (event in immediateQueue) {
        withTimeout(20.milliseconds) {
          block(event)
        }
      }
    }

    // Our second lot of processors are for long running tasks. These have no time limit
    // and are meant for longer running operations (anything which requires disk
    // or network access, etc)
    repeat(processors) {
      launch {
        for (event in longRunningQueue) {
          block(event)
        }
      }
    }
  }

  fun close() {
    immediateQueue.close()
    longRunningQueue.close()
  }
}

@Composable
fun <E : CircuitUiEvent> rememberEventQueue(): EventQueue<E> = remember {
  object : RememberObserver {
    val queue = EventQueue<E>()
    override fun onRemembered() = Unit
    override fun onAbandoned() = queue.close()
    override fun onForgotten() = queue.close()
  }.queue
}

@Composable
fun <E : CircuitUiEvent> LaunchedEventQueueProcessor(
  channel: EventQueue<E>,
  processors: Int = 3,
  block: suspend (E) -> Unit,
) {
  LaunchedEffect(channel, processors) {
    channel.run(processors, block)
  }
}
