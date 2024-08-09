// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.common.compose.ui.CheckboxPreference
import app.tivi.common.compose.ui.Preference
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.cd_navigate_up
import app.tivi.common.ui.resources.developer_settings_title
import app.tivi.screens.DevSettingsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

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
        title = { Text(stringResource(Res.string.developer_settings_title)) },
        navigationIcon = {
          IconButton(onClick = { eventSink(DevSettingsUiEvent.NavigateUp) }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBackForPlatform,
              contentDescription = stringResource(Res.string.cd_navigate_up),
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
          title = "Notifications",
          modifier = Modifier.clickable {
            state.eventSink(DevSettingsUiEvent.NavigateNotifications)
          },
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
