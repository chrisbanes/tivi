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

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import app.tivi.episodedetails.createEpisodeDetailsViewModel
import app.tivi.home.discover.Discover
import app.tivi.home.followed.Followed
import app.tivi.home.popular.Popular
import app.tivi.home.recommended.Recommended
import app.tivi.home.search.Search
import app.tivi.home.trending.Trending
import app.tivi.home.watched.Watched
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.seasons.ShowSeasons

internal sealed class Screen(val route: String) {
    object Discover : Screen("discover")
    object Following : Screen("following")
    object Watched : Screen("watched")
    object Search : Screen("search")
}

private sealed class LeafScreen(
    private val route: String
) {
    fun createRoute(root: Screen) = "${root.route}/$route"

    object Discover : LeafScreen("discover")
    object Following : LeafScreen("following")
    object Trending : LeafScreen("trending")
    object Popular : LeafScreen("popular")

    object ShowDetails : LeafScreen("show/{showId}") {
        fun createRoute(root: Screen, showId: Long): String {
            return "${root.route}/show/$showId"
        }
    }

    object EpisodeDetails : LeafScreen("episode/{episodeId}") {
        fun createRoute(root: Screen, episodeId: Long): String {
            return "${root.route}/episode/$episodeId"
        }
    }

    object ShowSeasons : LeafScreen("show/{showId}/seasons?seasonId={seasonId}") {
        fun createRoute(
            root: Screen,
            showId: Long,
            seasonId: Long? = null,
        ): String {
            return "${root.route}/show/$showId/seasons".let {
                if (seasonId != null) "$it?seasonId=$seasonId" else it
            }
        }
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
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()
    // Create a ViewModelStore, which is used to store and cancel ViewModels appropriately.
    val viewModelStore = remember(coroutineScope) {
        ViewModelStore(coroutineScope)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Discover.route,
        modifier = modifier,
    ) {
        addDiscoverTopLevel(navController, onOpenSettings, viewModelStore)
        addFollowingTopLevel(navController, onOpenSettings, viewModelStore)
        addWatchedTopLevel(navController, onOpenSettings, viewModelStore)
        addSearchTopLevel(navController, onOpenSettings, viewModelStore)
    }
}

private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
    viewModelStore: ViewModelStore,
) {
    navigation(
        route = Screen.Discover.route,
        startDestination = LeafScreen.Discover.createRoute(Screen.Discover)
    ) {
        addDiscover(navController, Screen.Discover)
        addAccount(navController, Screen.Discover, openSettings)
        addShowDetails(navController, Screen.Discover)
        addShowSeasons(navController, Screen.Discover)
        addEpisodeDetails(navController, Screen.Discover, viewModelStore)
        addRecommendedShows(navController, Screen.Discover)
        addTrendingShows(navController, Screen.Discover)
        addPopularShows(navController, Screen.Discover)
    }
}

private fun NavGraphBuilder.addFollowingTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
    viewModelStore: ViewModelStore,
) {
    navigation(
        route = Screen.Following.route,
        startDestination = LeafScreen.Following.createRoute(Screen.Following)
    ) {
        addFollowedShows(navController, Screen.Following)
        addAccount(navController, Screen.Following, openSettings)
        addShowDetails(navController, Screen.Following)
        addShowSeasons(navController, Screen.Following)
        addEpisodeDetails(navController, Screen.Following, viewModelStore)
    }
}

private fun NavGraphBuilder.addWatchedTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
    viewModelStore: ViewModelStore,
) {
    navigation(
        route = Screen.Watched.route,
        startDestination = LeafScreen.Watched.createRoute(Screen.Watched)
    ) {
        addWatchedShows(navController, Screen.Watched)
        addAccount(navController, Screen.Watched, openSettings)
        addShowDetails(navController, Screen.Watched)
        addShowSeasons(navController, Screen.Watched)
        addEpisodeDetails(navController, Screen.Watched, viewModelStore)
    }
}

private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
    viewModelStore: ViewModelStore,
) {
    navigation(
        route = Screen.Search.route,
        startDestination = LeafScreen.Search.createRoute(Screen.Search)
    ) {
        addSearch(navController, Screen.Search)
        addAccount(navController, Screen.Search, openSettings)
        addShowDetails(navController, Screen.Search)
        addShowSeasons(navController, Screen.Search)
        addEpisodeDetails(navController, Screen.Search, viewModelStore)
    }
}

private fun NavGraphBuilder.addDiscover(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Discover.createRoute(root)) {
        Discover(
            openTrendingShows = {
                navController.navigate(LeafScreen.Trending.createRoute(root))
            },
            openPopularShows = {
                navController.navigate(LeafScreen.Popular.createRoute(root))
            },
            openRecommendedShows = {
                navController.navigate(LeafScreen.RecommendedShows.createRoute(root))
            },
            openShowDetails = { showId, seasonId, episodeId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))

                // If we have an season id, we also open that
                if (seasonId != null) {
                    navController.navigate(
                        LeafScreen.ShowSeasons.createRoute(root, showId, seasonId)
                    )
                }
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate(LeafScreen.EpisodeDetails.createRoute(root, episodeId))
                }
            },
            openUser = {
                navController.navigate(LeafScreen.Account.createRoute(root))
            },
        )
    }
}

private fun NavGraphBuilder.addFollowedShows(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Following.createRoute(root)) {
        Followed(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.createRoute(root))
            },
        )
    }
}

private fun NavGraphBuilder.addWatchedShows(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Watched.createRoute(root)) {
        Watched(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            openUser = {
                navController.navigate(LeafScreen.Account.createRoute(root))
            },
        )
    }
}

private fun NavGraphBuilder.addSearch(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Search.createRoute(root)) {
        Search(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
        )
    }
}

private fun NavGraphBuilder.addShowDetails(
    navController: NavController,
    root: Screen,
) {
    composable(
        route = LeafScreen.ShowDetails.createRoute(root),
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType }
        )
    ) {
        ShowDetails(
            navigateUp = navController::navigateUp,
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(root, episodeId))
            },
            openSeasons = { showId, seasonId ->
                navController.navigate(LeafScreen.ShowSeasons.createRoute(root, showId, seasonId))
            }
        )
    }
}

private fun NavGraphBuilder.addEpisodeDetails(
    navController: NavController,
    root: Screen,
    viewModelStore: ViewModelStore,
) {
    composable(
        route = LeafScreen.EpisodeDetails.createRoute(root),
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        )
    ) { backStackEntry ->
        val activity = LocalContext.current as Activity
        val id = backStackEntry.arguments!!.getLong("episodeId")

        // Collect our ViewModel from the store. The key must be unique to the ViewModel
        // and its parameters. ViewModels should use the CoroutineScope provided to them when
        // launching coroutines
        val viewModel = viewModelStore.viewModel(
            key = "episode_details_$id",
            navBackStackEntry = backStackEntry,
        ) { scope ->
            createEpisodeDetailsViewModel(
                episodeId = id,
                activity = activity,
                coroutineScope = scope,
            )
        }

        EpisodeDetails(
            viewModel = viewModel,
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addRecommendedShows(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.RecommendedShows.createRoute(root)) {
        Recommended(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addTrendingShows(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Trending.createRoute(root)) {
        Trending(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addPopularShows(
    navController: NavController,
    root: Screen,
) {
    composable(LeafScreen.Popular.createRoute(root)) {
        Popular(
            openShowDetails = { showId ->
                navController.navigate(LeafScreen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addAccount(
    navController: NavController,
    root: Screen,
    onOpenSettings: () -> Unit,
) {
    composable(LeafScreen.Account.createRoute(root)) {
        // This should really be a dialog, but we're waiting on:
        // https://issuetracker.google.com/179608120
        Dialog(onDismissRequest = navController::navigateUp) {
            AccountUi(navController, onOpenSettings)
        }
    }
}

private fun NavGraphBuilder.addShowSeasons(
    navController: NavController,
    root: Screen,
) {
    composable(
        route = LeafScreen.ShowSeasons.createRoute(root),
        arguments = listOf(
            navArgument("showId") {
                type = NavType.LongType
            },
            navArgument("seasonId") {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        ShowSeasons(
            navigateUp = navController::navigateUp,
            openEpisodeDetails = { episodeId ->
                navController.navigate(LeafScreen.EpisodeDetails.createRoute(root, episodeId))
            },
            initialSeasonId = it.arguments?.getString("seasonId")?.toLong()
        )
    }
}
