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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigate
import androidx.navigation.compose.popUpTo
import androidx.navigation.compose.rememberNavController
import app.tivi.R
import app.tivi.Screen
import app.tivi.account.AccountUi
import app.tivi.account.AccountUiViewModel
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.episodedetails.EpisodeDetailsViewModel
import app.tivi.home.discover.Discover
import app.tivi.home.discover.DiscoverViewModel
import app.tivi.home.followed.Followed
import app.tivi.home.followed.FollowedViewModel
import app.tivi.home.popular.Popular
import app.tivi.home.popular.PopularShowsViewModel
import app.tivi.home.recommended.Recommended
import app.tivi.home.recommended.RecommendedShowsViewModel
import app.tivi.home.search.Search
import app.tivi.home.search.SearchViewModel
import app.tivi.home.trending.Trending
import app.tivi.home.trending.TrendingShowsViewModel
import app.tivi.home.watched.Watched
import app.tivi.home.watched.WatchedViewModel
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.details.ShowDetailsViewModel
import com.google.accompanist.insets.navigationBarsPadding

@Composable
internal fun Home() {
    Column {
        var currentSelectedItem by remember { mutableStateOf(Screen.Discover) }
        val navController = rememberNavController()

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Discover.route
            ) {
                composable(Screen.Discover.route) {
                    val viewModel: DiscoverViewModel = hiltNavGraphViewModel(it)
                    Discover(viewModel, navController)
                }
                composable(Screen.Following.route) {
                    val viewModel: FollowedViewModel = hiltNavGraphViewModel(it)
                    Followed(viewModel, navController)
                }
                composable(Screen.Watched.route) {
                    val viewModel: WatchedViewModel = hiltNavGraphViewModel(it)
                    Watched(viewModel, navController)
                }
                composable(Screen.Search.route) {
                    val viewModel: SearchViewModel = hiltNavGraphViewModel(it)
                    Search(viewModel, navController)
                }
                composable(Screen.ShowDetails.route) {
                    val viewModel: ShowDetailsViewModel = hiltNavGraphViewModel(it)
                    ShowDetails(viewModel, navController)
                }
                composable(Screen.RecommendedShows.route) {
                    val viewModel: RecommendedShowsViewModel = hiltNavGraphViewModel(it)
                    Recommended(viewModel, navController)
                }
                composable(Screen.Trending.route) {
                    val viewModel: TrendingShowsViewModel = hiltNavGraphViewModel(it)
                    Trending(viewModel, navController)
                }
                composable(Screen.Popular.route) {
                    val viewModel: PopularShowsViewModel = hiltNavGraphViewModel(it)
                    Popular(viewModel, navController)
                }
                composable(Screen.EpisodeDetails.route) {
                    val viewModel: EpisodeDetailsViewModel = hiltNavGraphViewModel(it)
                    EpisodeDetails(viewModel, navController)
                }
                composable(Screen.Account.route) {
                    // This should really be a dialog, but we're waiting on:
                    // https://issuetracker.google.com/179608120
                    val viewModel: AccountUiViewModel = hiltNavGraphViewModel(it)
                    AccountUi(viewModel, navController)
                }
                // TODO: Settings
            }
        }

        HomeBottomNavigation(
            selectedNavigation = currentSelectedItem,
            onNavigationSelected = { selected ->
                currentSelectedItem = selected
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
}

@Composable
internal fun HomeBottomNavigation(
    selectedNavigation: Screen,
    onNavigationSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface,
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
