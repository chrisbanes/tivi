// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.settings.developer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.ui.PreferenceHeader
import app.tivi.screens.DevSettingsScreen
import com.moriatsushi.insetsx.systemBars
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun DevSettings(
    state: DevSettingsUiState,
    modifier: Modifier = Modifier,
) {
    // Need to extract the eventSink out to a local val, so that the Compose Compiler
    // treats it as stable. See: https://issuetracker.google.com/issues/256100927
    val eventSink = state.eventSink

    Scaffold(
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
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            )
        },
        contentWindowInsets = WindowInsets.systemBars,
        modifier = modifier,
    ) { contentPadding ->
        LazyColumn(
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxWidth(),
        ) {
            stickyHeader {
                PreferenceHeader(LocalStrings.current.settingsUiCategoryTitle)
            }
        }
    }
}
