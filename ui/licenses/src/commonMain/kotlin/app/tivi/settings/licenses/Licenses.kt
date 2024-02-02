// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.licenses

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.ui.Preference
import app.tivi.common.compose.ui.PreferenceHeader
import app.tivi.common.compose.ui.TopAppBar
import app.tivi.screens.LicensesScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject

@Inject
class LicensesUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is LicensesScreen -> {
      ui<LicensesUiState> { state, modifier ->
        Licenses(state, modifier)
      }
    }

    else -> null
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun Licenses(
  state: LicensesUiState,
  modifier: Modifier = Modifier,
) {
  val eventSink = state.eventSink

  HazeScaffold(
    topBar = {
      TopAppBar(
        title = { Text(LocalStrings.current.settingsOpenSource) },
        navigationIcon = {
          IconButton(onClick = { eventSink(LicensesUiEvent.NavigateUp) }) {
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
      modifier = Modifier
        .padding(contentPadding)
        .fillMaxWidth(),
    ) {
      state.licenses.forEach { group ->
        stickyHeader {
          PreferenceHeader(
            title = group.id,
            modifier = Modifier.fillMaxSize(),
            tonalElevation = 1.dp,
          )
        }

        items(group.artifacts) { artifact ->
          Preference(
            title = (artifact.name ?: artifact.artifactId),
            summary = {
              Column {
                Text("${artifact.artifactId} v${artifact.version}")

                artifact.spdxLicenses?.forEach { license ->
                  Text(license.name)
                }
              }
            },
            modifier = Modifier.clickable {
              eventSink(LicensesUiEvent.NavigateRepository(artifact))
            },
          )
        }
      }
    }
  }
}
