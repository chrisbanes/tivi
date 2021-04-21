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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import androidx.navigation.compose.rememberNavController
import app.tivi.R
import app.tivi.Screen
import app.tivi.account.AccountUi
import app.tivi.common.compose.Scaffold
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.home.discover.Discover
import app.tivi.home.followed.Followed
import app.tivi.home.popular.Popular
import app.tivi.home.recommended.Recommended
import app.tivi.home.search.Search
import app.tivi.home.trending.Trending
import app.tivi.home.watched.Watched
import app.tivi.showdetails.details.ShowDetails
import com.google.accompanist.insets.navigationBarsPadding

@Composable
internal fun Home(
    onOpenSettings: () -> Unit,
) {
    val navController = rememberNavController()
    val currentSelectedItem by navController.currentScreenAsState()

    Scaffold(
        bottomBar = {
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
            NavHost(
                navController = navController,
                startDestination = Screen.Discover.route
            ) {
                composable(Screen.Discover.route) {
                    Discover(navController)
                }
                composable(Screen.Following.route) {
                    Followed(navController)
                }
                composable(Screen.Watched.route) {
                    Watched(navController)
                }
                composable(Screen.Search.route) {
                    Search(navController)
                }
                composable(
                    route = Screen.ShowDetails.route,
                    arguments = listOf(navArgument("showId") { type = NavType.LongType })
                ) {
                    ShowDetails(navController)
                }
                composable(Screen.RecommendedShows.route) {
                    Recommended(navController)
                }
                composable(Screen.Trending.route) {
                    Trending(navController)
                }
                composable(Screen.Popular.route) {
                    Popular(navController)
                }
                composable(
                    route = Screen.EpisodeDetails.route,
                    arguments = listOf(navArgument("episodeId") { type = NavType.LongType })
                ) {
                    EpisodeDetails(navController)
                }
                composable(Screen.Account.route) {
                    // This should really be a dialog, but we're waiting on:
                    // https://issuetracker.google.com/179608120
                    AccountUi(navController, onOpenSettings)
                }
            }
        }
    }
}

/**
 * Returns true if this [NavDestination] matches the given route.
 */
private fun NavDestination.matchesRoute(route: String): Boolean {
    // Copied from Compose-Navigation NavGraphBuilder.kt
    return hasDeepLink("android-app://androidx.navigation.compose/$route".toUri())
}

/**
 * Adds an [NavController.OnDestinationChangedListener] to this [NavController] and updates the return [State]
 * as the destination changes.
 */
@Composable
private fun NavController.currentScreenAsState(): State<Screen> {
    val selectedItem = remember { mutableStateOf(Screen.Discover) }

    DisposableEffect(this) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            when {
                destination.matchesRoute(Screen.Discover.route) -> {
                    selectedItem.value = Screen.Discover
                }
                destination.matchesRoute(Screen.Watched.route) -> {
                    selectedItem.value = Screen.Watched
                }
                destination.matchesRoute(Screen.Following.route) -> {
                    selectedItem.value = Screen.Following
                }
                destination.matchesRoute(Screen.Search.route) -> {
                    selectedItem.value = Screen.Search
                }
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
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_weekend_black_24dp),
                        contentDescription = stringResource(R.string.cd_discover_title)
                    )
                },
                label = { Text(stringResource(R.string.discover_title)) },
                selected = selectedNavigation == Screen.Discover,
                onClick = { onNavigationSelected(Screen.Discover) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(R.string.cd_following_shows_title)
                    )
                },
                label = { Text(stringResource(R.string.following_shows_title)) },
                selected = selectedNavigation == Screen.Following,
                onClick = { onNavigationSelected(Screen.Following) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_visibility),
                        contentDescription = stringResource(R.string.cd_watched_shows_title)
                    )
                },
                label = { Text(stringResource(R.string.watched_shows_title)) },
                selected = selectedNavigation == Screen.Watched,
                onClick = { onNavigationSelected(Screen.Watched) },
            )

            BottomNavigationItem(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.cd_search_navigation_title)
                    )
                },
                label = { Text(stringResource(R.string.search_navigation_title)) },
                selected = selectedNavigation == Screen.Search,
                onClick = { onNavigationSelected(Screen.Search) },
            )
        }
    }
}
