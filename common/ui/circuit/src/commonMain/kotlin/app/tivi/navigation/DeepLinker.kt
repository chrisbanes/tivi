// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.Snapshot
import app.tivi.inject.ApplicationScope
import app.tivi.screens.DiscoverScreen
import app.tivi.screens.EpisodeDetailsScreen
import app.tivi.screens.ShowDetailsScreen
import app.tivi.screens.ShowSeasonsScreen
import com.eygraber.uri.Uri
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.resetRoot
import com.slack.circuit.runtime.screen.Screen
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
@Stable
class DeepLinker {

  private val _pending by lazy {
    MutableSharedFlow<Uri>(
      extraBufferCapacity = 1,
      onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
  }

  val pending: Flow<Uri> by lazy { _pending.asSharedFlow() }

  fun addDeeplink(deeplink: Uri) {
    _pending.tryEmit(deeplink)
  }

  fun addDeeplink(string: String) = addDeeplink(Uri.parse(string))
}

fun Navigator.applyDeeplink(deeplink: Uri) {
  var pendingScreen: String? = null
  val queued = mutableListOf<Screen>()

  for (segment in deeplink.pathSegments) {
    if (pendingScreen != null) {
      try {
        val id = segment.toLong()
        when (pendingScreen) {
          "show" -> queued.add(ShowDetailsScreen(id = id))
          "season" -> {
            val last = queued.last()
            if (last is ShowDetailsScreen) {
              queued.add(ShowSeasonsScreen(showId = last.id, selectedSeasonId = id))
            } else {
              error("Can't apply deeplink: $deeplink")
            }
          }
          "episode" -> queued.add(EpisodeDetailsScreen(id))
        }

        pendingScreen = null
      } catch (nfe: NumberFormatException) {
        error("Can't apply deeplink: $deeplink")
      }
    } else {
      pendingScreen = segment
    }
  }

  if (queued.isEmpty()) {
    // deep link didn't produce any screens, don't do anything
    return
  }

  Snapshot.withMutableSnapshot {
    resetRoot(DiscoverScreen)
    for (screen in queued) {
      goTo(screen)
    }
  }
}

@Composable
fun LaunchDeepLinker(
  deepLinker: DeepLinker,
  navigator: Navigator = LocalNavigator.current,
) {
  LaunchedEffect(deepLinker, navigator) {
    deepLinker.pending.collect { deeplink ->
      navigator.applyDeeplink(deeplink)
    }
  }
}
