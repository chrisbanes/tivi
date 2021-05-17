/*
 * Copyright 2020 Google LLC
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

package app.tivi.home

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import app.tivi.AppNavigation
import app.tivi.R
import app.tivi.Screen
import app.tivi.common.compose.Scaffold
import app.tivi.common.compose.theme.AppBarAlphas
import com.google.accompanist.insets.navigationBarsPadding

@Composable
internal fun Home(
    onOpenSettings: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val currentSelectedItem by navController.currentScreenAsState()

            HomeBottomNavigation(
                selectedNavigation = currentSelectedItem,
                onNavigationSelected = { selected ->
                    navController.navigate(selected.route) {
                        launchSingleTop = true
                        popUpTo(Screen.Discover.route) {
                            inclusive = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            AppNavigation(
                navController = navController,
                onOpenSettings = onOpenSettings
            )
        }
    }
}

/**
 * Adds an [NavController.OnDestinationChangedListener] to this [NavController] and updates the
 * returned [State] which is updated as the destination changes.
 */
@Stable
@Composable
private fun NavController.currentScreenAsState(): State<Screen> {
    val selectedItem = remember { mutableStateOf(Screen.Discover) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when (destination.route) {
                Screen.Discover.route -> selectedItem.value = Screen.Discover
                Screen.Watched.route -> selectedItem.value = Screen.Watched
                Screen.Following.route -> selectedItem.value = Screen.Following
                Screen.Search.route -> selectedItem.value = Screen.Search
                // We intentionally ignore any other destinations, as they're likely to be
                // leaf destinations.
            }
        }
        addOnDestinationChangedListener(listener)

        onDispose {
            removeOnDestinationChangedListener(listener)
        }
    }

    return selectedItem
}

@Composable
internal fun HomeBottomNavigation(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = AppBarAlphas.translucentBarAlpha()),
        contentColor = contentColorFor(MaterialTheme.colors.surface),
        elevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            Modifier
                .navigationBarsPadding()
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HomeBottomNavigationItem(
                label = stringResource(R.string.discover_title),
                selected = selectedNavigation == Screen.Discover,
                onClick = { onNavigationSelected(Screen.Discover) },
                contentDescription = stringResource(R.string.cd_discover_title),
                selectedPainter = painterResource(R.drawable.ic_weekend_filled),
                painter = painterResource(R.drawable.ic_weekend_outline),
            )

            HomeBottomNavigationItem(
                label = stringResource(R.string.following_shows_title),
                selected = selectedNavigation == Screen.Following,
                onClick = { onNavigationSelected(Screen.Following) },
                contentDescription = stringResource(R.string.cd_following_shows_title),
                selectedPainter = rememberVectorPainter(Icons.Default.Favorite),
                painter = rememberVectorPainter(Icons.Default.FavoriteBorder),
            )

            HomeBottomNavigationItem(
                label = stringResource(R.string.watched_shows_title),
                selected = selectedNavigation == Screen.Watched,
                onClick = { onNavigationSelected(Screen.Watched) },
                contentDescription = stringResource(R.string.cd_watched_shows_title),
                selectedPainter = painterResource(R.drawable.ic_visibility),
                painter = painterResource(R.drawable.ic_visibility_outline),
            )

            HomeBottomNavigationItem(
                label = stringResource(R.string.search_navigation_title),
                selected = selectedNavigation == Screen.Search,
                onClick = { onNavigationSelected(Screen.Search) },
                contentDescription = stringResource(R.string.cd_search_navigation_title),
                painter = rememberVectorPainter(Icons.Default.Search),
            )
        }
    }
}

@Composable
private fun RowScope.HomeBottomNavigationItem(
    selected: Boolean,
    selectedPainter: Painter? = null,
    painter: Painter,
    contentDescription: String,
    label: String,
    onClick: () -> Unit,
) {
    BottomNavigationItem(
        icon = {
            if (selectedPainter != null) {
                Crossfade(targetState = selected) { selected ->
                    Icon(
                        painter = if (selected) selectedPainter else painter,
                        contentDescription = contentDescription
                    )
                }
            } else {
                Icon(
                    painter = painter,
                    contentDescription = contentDescription
                )
            }
        },
        label = { Text(label) },
        selected = selected,
        onClick = onClick,
    )
}
