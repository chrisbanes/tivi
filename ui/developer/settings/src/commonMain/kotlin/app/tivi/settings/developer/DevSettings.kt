// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.ui.CheckboxPreference
import app.tivi.common.compose.ui.Preference
import app.tivi.common.compose.ui.TopAppBar
import app.tivi.screens.DevSettingsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject

@Inject
class DevSettingsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is DevSettingsScreen -> {
      ui<DevSettingsUiState> { state, modifier ->
        DevSettings(state, modifier)
      }
    }

    else -> null
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DevSettings(
  state: DevSettingsUiState,
  modifier: Modifier = Modifier,
) {
  val eventSink = state.eventSink

  HazeScaffold(
    topBar = {
      TopAppBar(
        title = { Text(LocalStrings.current.developerSettingsTitle) },
        navigationIcon = {
          IconButton(onClick = { eventSink(DevSettingsUiEvent.NavigateUp) }) {
            Icon(
              imageVector = Icons.Default.ArrowBack,
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
      modifier = Modifier.fillMaxWidth(),
    ) {
      item {
        CheckboxPreference(
          checked = state.hideArtwork,
          title = "Hide artwork",
          onCheckClicked = { state.eventSink(DevSettingsUiEvent.ToggleHideArtwork) },
        )
      }

      item {
        Preference(
          title = "Open log",
          modifier = Modifier.clickable {
            state.eventSink(DevSettingsUiEvent.NavigateLog)
          },
        )
      }
    }
  }
}
