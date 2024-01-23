// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.tivi.data.models.TraktUser
import io.github.alexzhirkevich.cupertino.CupertinoTopAppBarDefaults
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTopAppBar
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi

@ExperimentalMaterial3Api
@Composable
fun TiviRootScreenAppBar(
  title: String,
  loggedIn: Boolean,
  user: TraktUser?,
  refreshing: Boolean,
  onRefreshActionClick: () -> Unit,
  onUserActionClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    modifier = modifier,
    transparent = true,
    title = { Text(text = title) },
    actions = {
      // This button refresh allows screen-readers, etc to trigger a refresh.
      // We only show the button to trigger a refresh, not to indicate that
      // we're currently refreshing, otherwise we have 4 indicators showing the
      // same thing.
      Crossfade(
        targetState = refreshing,
        modifier = Modifier.align(Alignment.CenterVertically),
      ) { isRefreshing ->
        if (!isRefreshing) {
          RefreshButton(onClick = onRefreshActionClick)
        }
      }

      UserProfileButton(
        loggedIn = loggedIn,
        user = user,
        onClick = onUserActionClick,
        modifier = Modifier.align(Alignment.CenterVertically),
      )
    },
  )
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class)
@Composable
fun TopAppBar(
  title: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  navigationIcon: @Composable () -> Unit = {},
  actions: @Composable (RowScope.() -> Unit) = {},
  windowInsets: WindowInsets = CupertinoTopAppBarDefaults.windowInsets,
  transparent: Boolean = false,
) {
  AdaptiveTopAppBar(
    title = title,
    modifier = modifier,
    navigationIcon = navigationIcon,
    actions = actions,
    windowInsets = windowInsets,
    adaptation = {
      material {
        if (transparent) {
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        }
      }
      cupertino {
        isTransparent = transparent
      }
    }
  )
}
