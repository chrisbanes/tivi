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

package app.tivi

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.navigation
import app.tivi.account.AccountUi
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.home.discover.Discover
import app.tivi.home.followed.Followed
import app.tivi.home.popular.Popular
import app.tivi.home.recommended.Recommended
import app.tivi.home.search.Search
import app.tivi.home.trending.Trending
import app.tivi.home.watched.Watched
import app.tivi.showdetails.details.ShowDetails

internal enum class Screen(val route: String) {
    Discover("discoverroot"),
    Following("followingroot"),
    Watched("watchedroot"),
    Search("searchroot"),
}

private enum class LeafScreens(val route: String) {
    Discover("discover"),
    Following("following"),
    Trending("trending"),
    Popular("popular"),
    ShowDetails("show/{showId}"),
    EpisodeDetails("episode/{episodeId}"),
    RecommendedShows("recommendedshows"),
    Watched("watched"),
    Search("search"),
    Account("account"),
}

@Composable
internal fun AppNavigation(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Discover.route
    ) {
        addDiscoverTopLevel(navController, onOpenSettings)
        addFollowingTopLevel(navController, onOpenSettings)
        addWatchedTopLevel(navController, onOpenSettings)
        addSearchTopLevel(navController, onOpenSettings)
    }
}

private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Discover.route,
        startDestination = LeafScreens.Discover.route
    ) {
        addDiscover(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addEpisodeDetails(navController)
        addRecommendedShows(navController)
        addTrendingShows(navController)
        addPopularShows(navController)
    }
}

private fun NavGraphBuilder.addFollowingTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Following.route,
        startDestination = LeafScreens.Following.route
    ) {
        addFollowedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addWatchedTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Watched.route,
        startDestination = LeafScreens.Watched.route
    ) {
        addWatchedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Search.route,
        startDestination = LeafScreens.Search.route
    ) {
        addSearch(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addDiscover(navController: NavController) {
    composable(LeafScreens.Discover.route) {
        Discover(
            openTrendingShows = {
                navController.navigate(LeafScreens.Trending.route)
            },
            openPopularShows = {
                navController.navigate(LeafScreens.Popular.route)
            },
            openRecommendedShows = {
                navController.navigate(LeafScreens.RecommendedShows.route)
            },
            openShowDetails = { showId, episodeId ->
                navController.navigate("show/$showId")
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate("episode/$episodeId")
                }
            },
            openUser = {
                navController.navigate(LeafScreens.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addFollowedShows(navController: NavController) {
    composable(LeafScreens.Following.route) {
        Followed(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
            openUser = {
                navController.navigate(LeafScreens.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addWatchedShows(navController: NavController) {
    composable(LeafScreens.Watched.route) {
        Watched(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
            openUser = {
                navController.navigate(LeafScreens.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composable(LeafScreens.Search.route) {
        Search(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addShowDetails(navController: NavController) {
    composable(
        route = LeafScreens.ShowDetails.route,
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType }
        )
    ) {
        ShowDetails(
            navigateUp = {
                navController.popBackStack()
            },
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
            openEpisodeDetails = { episodeId ->
                navController.navigate("episode/$episodeId")
            }
        )
    }
}

private fun NavGraphBuilder.addEpisodeDetails(navController: NavController) {
    composable(
        route = LeafScreens.EpisodeDetails.route,
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType }
        )
    ) {
        EpisodeDetails(
            navigateUp = {
                navController.popBackStack()
            },
        )
    }
}

private fun NavGraphBuilder.addRecommendedShows(navController: NavController) {
    composable(LeafScreens.RecommendedShows.route) {
        Recommended(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addTrendingShows(navController: NavController) {
    composable(LeafScreens.Trending.route) {
        Trending(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addPopularShows(navController: NavController) {
    composable(LeafScreens.Popular.route) {
        Popular(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addAccount(
    navController: NavController,
    onOpenSettings: () -> Unit,
) {
    composable(LeafScreens.Account.route) {
        // This should really be a dialog, but we're waiting on:
        // https://issuetracker.google.com/179608120
        AccountUi(navController, onOpenSettings)
    }
}
