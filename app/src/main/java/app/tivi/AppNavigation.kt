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
        addDiscover(navController)
        addFollowedShows(navController)
        addWatchedShows(navController)
        addShowDetails(navController)
        addEpisodeDetails(navController)
        addRecommendedShows(navController)
        addTrendingShows(navController)
        addPopularShows(navController)
        addAccount(navController, onOpenSettings)
        addSearch(navController)
    }
}

private fun NavGraphBuilder.addDiscover(navController: NavController) {
    composable(Screen.Discover.route) {
        Discover(
            openTrendingShows = {
                navController.navigate(Screen.Trending.route)
            },
            openPopularShows = {
                navController.navigate(Screen.Popular.route)
            },
            openRecommendedShows = {
                navController.navigate(Screen.RecommendedShows.route)
            },
            openShowDetails = { showId, episodeId ->
                navController.navigate("show/$showId")
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate("episode/$episodeId")
                }
            },
            openUser = {
                navController.navigate(Screen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addFollowedShows(navController: NavController) {
    composable(Screen.Following.route) {
        Followed(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
            openUser = {
                navController.navigate(Screen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addWatchedShows(navController: NavController) {
    composable(Screen.Watched.route) {
        Watched(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
            openUser = {
                navController.navigate(Screen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composable(Screen.Search.route) {
        Search(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addShowDetails(navController: NavController) {
    composable(
        route = Screen.ShowDetails.route,
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
        route = Screen.EpisodeDetails.route,
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
    composable(Screen.RecommendedShows.route) {
        Recommended(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addTrendingShows(navController: NavController) {
    composable(Screen.Trending.route) {
        Trending(
            openShowDetails = { showId ->
                navController.navigate("show/$showId")
            },
        )
    }
}

private fun NavGraphBuilder.addPopularShows(navController: NavController) {
    composable(Screen.Popular.route) {
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
    composable(Screen.Account.route) {
        // This should really be a dialog, but we're waiting on:
        // https://issuetracker.google.com/179608120
        AccountUi(navController, onOpenSettings)
    }
}
