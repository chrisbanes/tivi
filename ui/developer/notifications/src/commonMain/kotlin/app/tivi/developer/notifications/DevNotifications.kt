// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.developer.notifications

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.common.compose.ui.Preference
import app.tivi.common.compose.ui.PreferenceDivider
import app.tivi.common.compose.ui.PreferenceHeader
import app.tivi.screens.DevNotificationsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject

@Inject
class DevNotificationsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is DevNotificationsScreen -> {
      ui<DevNotificationsUiState> { state, modifier ->
        DevNotifications(state, modifier)
      }
    }

    else -> null
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun DevNotifications(
  state: DevNotificationsUiState,
  modifier: Modifier = Modifier,
) {
  val eventSink = state.eventSink

  HazeScaffold(
    topBar = {
      TopAppBar(
        title = { Text("Notifications") },
        navigationIcon = {
          IconButton(onClick = { eventSink(DevNotificationsUiEvent.NavigateUp) }) {
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
      modifier = Modifier.fillMaxWidth(),
    ) {
      stickyHeader {
        PreferenceHeader("Actions")
      }

      item {
        Preference(
          title = "Show episode airing notification",
          modifier = Modifier.clickable {
            state.eventSink(DevNotificationsUiEvent.ShowEpisodeAiringNotification)
          },
        )
      }

      item {
        Preference(
          title = "Show notification (in 5 seconds)",
          modifier = Modifier.clickable {
            state.eventSink(DevNotificationsUiEvent.ShowNotification)
          },
        )
      }

      item {
        Preference(
          title = "Schedule notification (in 15 mins)",
          modifier = Modifier.clickable {
            state.eventSink(DevNotificationsUiEvent.ScheduleNotification)
          },
        )
      }

      item { PreferenceDivider() }

      itemSpacer(16.dp)

      stickyHeader {
        PreferenceHeader("Pending Notifications")
      }

      items(state.pendingNotifications) { notification ->
        ListItem(
          overlineContent = {
            Text(LocalTiviDateFormatter.current.formatMediumDateTime(notification.date))
          },
          headlineContent = { Text(notification.title) },
          supportingContent = {
            Column {
              Text("Message: ${notification.message}")
              Text("Deeplink: ${notification.deeplinkUrl}")
              Text("Channel: ${notification.channel}")
            }
          },
        )
      }
    }
  }
}
