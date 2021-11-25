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

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.dialog
import androidx.navigation.navArgument
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
import app.tivi.showdetails.seasons.ShowSeasons
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.navigation

internal sealed class Screen(val route: String) {
    object Discover : Screen("discover")
    object Following : Screen("following")
    object Watched : Screen("watched")
    object Search : Screen("search")
}

private sealed class LeafScreen(
    private val route: String,
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

@ExperimentalAnimationApi
@Composable
internal fun AppNavigation(
    navController: NavHostController,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedNavHost(
        navController = navController,
        startDestination = Screen.Discover.route,
        enterTransition = { defaultTiviEnterTransition(initialState, targetState) },
        exitTransition = { defaultTiviExitTransition(initialState, targetState) },
        popEnterTransition = { defaultTiviPopEnterTransition() },
        popExitTransition = { defaultTiviPopExitTransition() },
        modifier = modifier,
    ) {
        addDiscoverTopLevel(navController, onOpenSettings)
        addFollowingTopLevel(navController, onOpenSettings)
        addWatchedTopLevel(navController, onOpenSettings)
        addSearchTopLevel(navController, onOpenSettings)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Discover.route,
        startDestination = LeafScreen.Discover.createRoute(Screen.Discover),
    ) {
        addDiscover(navController, Screen.Discover)
        addAccount(navController, Screen.Discover, openSettings)
        addShowDetails(navController, Screen.Discover)
        addShowSeasons(navController, Screen.Discover)
        addEpisodeDetails(navController, Screen.Discover)
        addRecommendedShows(navController, Screen.Discover)
        addTrendingShows(navController, Screen.Discover)
        addPopularShows(navController, Screen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addFollowingTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Following.route,
        startDestination = LeafScreen.Following.createRoute(Screen.Following),
    ) {
        addFollowedShows(navController, Screen.Following)
        addAccount(navController, Screen.Following, openSettings)
        addShowDetails(navController, Screen.Following)
        addShowSeasons(navController, Screen.Following)
        addEpisodeDetails(navController, Screen.Following)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addWatchedTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Watched.route,
        startDestination = LeafScreen.Watched.createRoute(Screen.Watched),
    ) {
        addWatchedShows(navController, Screen.Watched)
        addAccount(navController, Screen.Watched, openSettings)
        addShowDetails(navController, Screen.Watched)
        addShowSeasons(navController, Screen.Watched)
        addEpisodeDetails(navController, Screen.Watched)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = Screen.Search.route,
        startDestination = LeafScreen.Search.createRoute(Screen.Search),
    ) {
        addSearch(navController, Screen.Search)
        addAccount(navController, Screen.Search, openSettings)
        addShowDetails(navController, Screen.Search)
        addShowSeasons(navController, Screen.Search)
        addEpisodeDetails(navController, Screen.Search)
    }
}

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
private fun NavGraphBuilder.addEpisodeDetails(
    navController: NavController,
    root: Screen,
) {
    composable(
        route = LeafScreen.EpisodeDetails.createRoute(root),
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        )
    ) {
        EpisodeDetails(
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
private fun NavGraphBuilder.addAccount(
    navController: NavController,
    root: Screen,
    onOpenSettings: () -> Unit,
) {
    dialog(LeafScreen.Account.createRoute(root)) {
        AccountUi(navController, onOpenSettings)
    }
}

@ExperimentalAnimationApi
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

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviEnterTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): EnterTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeIn()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.Start)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviExitTransition(
    initial: NavBackStackEntry,
    target: NavBackStackEntry,
): ExitTransition {
    val initialNavGraph = initial.destination.hostNavGraph
    val targetNavGraph = target.destination.hostNavGraph
    // If we're crossing nav graphs (bottom navigation graphs), we crossfade
    if (initialNavGraph.id != targetNavGraph.id) {
        return fadeOut()
    }
    // Otherwise we're in the same nav graph, we can imply a direction
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.Start)
}

private val NavDestination.hostNavGraph: NavGraph
    get() = hierarchy.first { it is NavGraph } as NavGraph

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviPopEnterTransition(): EnterTransition {
    return fadeIn() + slideIntoContainer(AnimatedContentScope.SlideDirection.End)
}

@ExperimentalAnimationApi
private fun AnimatedContentScope<*>.defaultTiviPopExitTransition(): ExitTransition {
    return fadeOut() + slideOutOfContainer(AnimatedContentScope.SlideDirection.End)
}
