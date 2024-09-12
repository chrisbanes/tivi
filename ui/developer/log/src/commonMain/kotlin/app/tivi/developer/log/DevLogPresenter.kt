// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.log

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.tivi.screens.DevLogScreen
import app.tivi.util.RecordingLoggerWriter
import app.tivi.wrapEventSink
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
class DevLogUiPresenterFactory(
  private val presenterFactory: (Navigator) -> DevLogPresenter,
) : Presenter.Factory {
  override fun create(
    screen: Screen,
    navigator: Navigator,
    context: CircuitContext,
  ): Presenter<*>? = when (screen) {
    is DevLogScreen -> presenterFactory(navigator)
    else -> null
  }
}

@Inject
class DevLogPresenter(
  @Assisted private val navigator: Navigator,
) : Presenter<DevLogUiState> {

  @Composable
  override fun present(): DevLogUiState {
    val logs by remember {
      RecordingLoggerWriter.buffer.map { it.asReversed() }
    }.collectAsState(emptyList())

    val eventSink: CoroutineScope.(DevLogUiEvent) -> Unit = { event ->
      when (event) {
        DevLogUiEvent.NavigateUp -> navigator.pop()
      }
    }

    return DevLogUiState(
      logs = logs,
      eventSink = wrapEventSink(eventSink),
    )
  }
}
