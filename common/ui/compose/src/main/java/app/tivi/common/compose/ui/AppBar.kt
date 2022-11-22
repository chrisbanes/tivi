/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.common.compose.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.primarySurface
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tivi.data.entities.TraktUser

/**
 * A wrapper around [TopAppBar] which allows some [bottomContent] below the bar, but within the same
 * surface. This is useful for tabs.
 */
@Composable
fun TopAppBarWithBottomContent(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation,
    contentPadding: PaddingValues? = null
) {
    Surface(
        color = backgroundColor,
        elevation = elevation,
        contentColor = contentColor,
        modifier = modifier
    ) {
        Column(contentPadding?.let { Modifier.padding(it) } ?: Modifier) {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                backgroundColor = Color.Transparent,
                contentColor = LocalContentColor.current,
                elevation = 0.dp
            )

            bottomContent?.invoke()
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun TiviStandardAppBar(
    title: String,
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.TopAppBar(
        modifier = modifier,
        title = { Text(text = title) },
        actions = {
            // This button refresh allows screen-readers, etc to trigger a refresh.
            // We only show the button to trigger a refresh, not to indicate that
            // we're currently refreshing, otherwise we have 4 indicators showing the
            // same thing.
            Crossfade(
                targetState = refreshing,
                modifier = Modifier.align(Alignment.CenterVertically)
            ) { isRefreshing ->
                if (!isRefreshing) {
                    RefreshButton(onClick = onRefreshActionClick)
                }
            }

            UserProfileButton(
                loggedIn = loggedIn,
                user = user,
                onClick = onUserActionClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    )
}
