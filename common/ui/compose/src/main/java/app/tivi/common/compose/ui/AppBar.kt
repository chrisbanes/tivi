// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tivi.data.models.TraktUser

/**
 * A wrapper around [TopAppBar] which allows some [bottomContent] below the bar, but within the same
 * surface. This is useful for tabs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithBottomContent(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit,
    bottomContent: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
    elevation: Dp = 0.dp,
) {
    Surface(
        color = containerColor,
        tonalElevation = elevation,
        contentColor = contentColor,
        modifier = modifier,
    ) {
        Column {
            TopAppBar(
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = LocalContentColor.current,
                    actionIconContentColor = LocalContentColor.current,
                ),
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
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
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
