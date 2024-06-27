// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.log

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.screens.DevLogScreen
import app.tivi.util.Severity
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject

@Inject
class DevLogUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is DevLogScreen -> {
      ui<DevLogUiState> { state, modifier ->
        DevLog(state, modifier)
      }
    }

    else -> null
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DevLog(
  state: DevLogUiState,
  modifier: Modifier = Modifier,
) {
  val eventSink = state.eventSink

  HazeScaffold(
    topBar = {
      TopAppBar(
        title = { Text("Log") },
        navigationIcon = {
          IconButton(onClick = { eventSink(DevLogUiEvent.NavigateUp) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBackForPlatform,
              contentDescription = LocalStrings.current.cdNavigateUp,
            )
          }
        },
      )
    },
    modifier = modifier,
  ) { contentPadding ->
    LazyColumn(
      contentPadding = contentPadding,
      verticalArrangement = Arrangement.spacedBy(2.dp),
      reverseLayout = true,
      modifier = Modifier.fillMaxWidth(),
    ) {
      items(state.logs) { logMessage ->
        Text(
          text = buildAnnotatedString {
            withStyle(
              style = SpanStyle(
                color = when (logMessage.severity) {
                  Severity.Verbose -> Color.Blue
                  Severity.Debug -> Color.Blue
                  Severity.Info -> Color.Yellow
                  Severity.Warn -> Color.Magenta
                  Severity.Error -> Color.Red
                  Severity.Assert -> Color.Red
                },
              ),
            ) {
              append(
                when (logMessage.severity) {
                  Severity.Verbose -> "[V]"
                  Severity.Debug -> "[D]"
                  Severity.Info -> "[I]"
                  Severity.Warn -> "[W]"
                  Severity.Error -> "[E]"
                  Severity.Assert -> "[A]"
                },
              )
            }
            append(' ')
            append(logMessage.message)
          },
          fontFamily = FontFamily.Monospace,
          fontSize = 12.sp,
          modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp),
        )

        if (logMessage.throwable != null) {
          Text(
            text = logMessage.throwable.toString(),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = modifier.padding(horizontal = 16.dp, vertical = 2.dp),
          )
        }

        HorizontalDivider()
      }
    }
  }
}
