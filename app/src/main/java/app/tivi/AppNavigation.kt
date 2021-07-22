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
import androidx.compose.ui.window.Dialog
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
import app.tivi.showdetails.season.SeasonDetails

internal sealed class Screen(val route: String) {
    object Discover : Screen("discoverroot")
    object Following : Screen("followingroot")
    object Watched : Screen("watchedroot")
    object Search : Screen("searchroot")
}

private sealed class LeafScreen(val route: String) {
    object Discover : LeafScreen("discover")
    object Following : LeafScreen("following")
    object Trending : LeafScreen("trending")
    object Popular : LeafScreen("popular")

    object ShowDetails : LeafScreen("show/{showId}") {
        fun createRoute(showId: Long): String = "show/$showId"
    }

    object EpisodeDetails : LeafScreen("episode/{episodeId}") {
        fun createRoute(episodeId: Long): String = "episode/$episodeId"
    }

    object Season : LeafScreen("season/{seasonId}") {
        fun createRoute(seasonId: Long): String = "season/$seasonId"
    }

    object RecommendedShows : LeafScreen("recommendedshows")
    object Watched : LeafScreen("watched")
    object Search : LeafScreen("search")
    object Account : LeafScreen("account")
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
        startDestination = LeafScreen.Discover.route
    ) {
        addDiscover(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addSeason(navController)
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
        startDestination = LeafScreen.Following.route
    ) {
        addFollowedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addSeason(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addWatchedTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Watched.route,
        startDestination = LeafScreen.Watched.route
    ) {
        addWatchedShows(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addSeason(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Search.route,
        startDestination = LeafScreen.Search.route
    ) {
        addSearch(navController)
        addAccount(navController, openSettings)
        addShowDetails(navController)
        addSeason(navController)
        addEpisodeDetails(navController)
    }
}

private fun NavGraphBuilder.addDiscover(navController: NavController) {
    composable(LeafScreen.Discover.route) {
        Discover(
            openTrendingShows = {
                navController.navigate(LeafScreen.Trending.route)
            },
            openPopularShows = {
                navController.navigate(LeafScreen.Popular.route)
            },
            openRecommendedShows = {
                navController.navigate(LeafScreen.RecommendedShows.route)
            },
            openShowDetails = { showId, episodeId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
                }
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addFollowedShows(navController: NavController) {
    composable(LeafScreen.Following.route) {
        Followed(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addWatchedShows(navController: NavController) {
    composable(LeafScreen.Watched.route) {
        Watched(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.route)
            },
        )
    }
}

private fun NavGraphBuilder.addSearch(navController: NavController) {
    composable(LeafScreen.Search.route) {
        Search(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
        )
    }
}

private fun NavGraphBuilder.addShowDetails(navController: NavController) {
    composable(
        route = LeafScreen.ShowDetails.route,
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType }
        )
    ) {
        ShowDetails(
            navigateUp = {
                navController.popBackStack()
            },
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
            },
            openSeasonDetails = { seasonId ->
                navController.navigate(LeafScreen.Season.createRoute(seasonId))
            }
        )
    }
}

private fun NavGraphBuilder.addEpisodeDetails(navController: NavController) {
    composable(
        route = LeafScreen.EpisodeDetails.route,
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
    composable(LeafScreen.RecommendedShows.route) {
        Recommended(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addTrendingShows(navController: NavController) {
    composable(LeafScreen.Trending.route) {
        Trending(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addPopularShows(navController: NavController) {
    composable(LeafScreen.Popular.route) {
        Popular(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addAccount(
    navController: NavController,
    onOpenSettings: () -> Unit,
) {
    composable(LeafScreen.Account.route) {
        // This should really be a dialog, but we're waiting on:
        // https://issuetracker.google.com/179608120
        Dialog(onDismissRequest = navController::popBackStack) {
            AccountUi(navController, onOpenSettings)
        }
    }
}

private fun NavGraphBuilder.addSeason(navController: NavController) {
    composable(
        route = LeafScreen.Season.route,
        arguments = listOf(
            navArgument("seasonId") { type = NavType.LongType }
        )
    ) {
        SeasonDetails(
            navigateUp = navController::navigateUp,
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(episodeId))
            },
        )
    }
}
