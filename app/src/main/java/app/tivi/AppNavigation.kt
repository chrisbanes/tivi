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
import androidx.compose.material.ExperimentalMaterialApi
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
import androidx.navigation.navArgument
import app.tivi.account.AccountUi
import app.tivi.episodedetails.EpisodeDetails
import app.tivi.home.discover.Discover
import app.tivi.home.library.Library
import app.tivi.home.popular.PopularShows
import app.tivi.home.recommended.RecommendedShows
import app.tivi.home.search.Search
import app.tivi.home.trending.TrendingShows
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.seasons.ShowSeasons
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi

internal sealed class RootScreen(val route: String) {
    object Discover : RootScreen("discover")
    object Library : RootScreen("library")
    object Search : RootScreen("search")
}

private sealed class Screen(
    private val route: String,
) {
    fun createRoute(root: RootScreen) = "${root.route}/$route"

    object Discover : Screen("discover")
    object Trending : Screen("trending")
    object Library : Screen("library")
    object Popular : Screen("popular")

    object ShowDetails : Screen("show/{showId}") {
        fun createRoute(root: RootScreen, showId: Long): String {
            return "${root.route}/show/$showId"
        }
    }

    object EpisodeDetails : Screen("episode/{episodeId}") {
        fun createRoute(root: RootScreen, episodeId: Long): String {
            return "${root.route}/episode/$episodeId"
        }
    }

    object ShowSeasons : Screen("show/{showId}/seasons?seasonId={seasonId}") {
        fun createRoute(
            root: RootScreen,
            showId: Long,
            seasonId: Long? = null,
        ): String {
            return "${root.route}/show/$showId/seasons".let {
                if (seasonId != null) "$it?seasonId=$seasonId" else it
            }
        }
    }

    object RecommendedShows : Screen("recommendedshows")
    object Search : Screen("search")
    object Account : Screen("account")
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
        startDestination = RootScreen.Discover.route,
        enterTransition = { defaultTiviEnterTransition(initialState, targetState) },
        exitTransition = { defaultTiviExitTransition(initialState, targetState) },
        popEnterTransition = { defaultTiviPopEnterTransition() },
        popExitTransition = { defaultTiviPopExitTransition() },
        modifier = modifier,
    ) {
        addDiscoverTopLevel(navController, onOpenSettings)
        addLibraryTopLevel(navController, onOpenSettings)
        addSearchTopLevel(navController, onOpenSettings)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Discover.route,
        startDestination = Screen.Discover.createRoute(RootScreen.Discover),
    ) {
        addDiscover(navController, RootScreen.Discover)
        addAccount(RootScreen.Discover, openSettings)
        addShowDetails(navController, RootScreen.Discover)
        addShowSeasons(navController, RootScreen.Discover)
        addEpisodeDetails(navController, RootScreen.Discover)
        addRecommendedShows(navController, RootScreen.Discover)
        addTrendingShows(navController, RootScreen.Discover)
        addPopularShows(navController, RootScreen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addLibraryTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Library.route,
        startDestination = Screen.Library.createRoute(RootScreen.Library),
    ) {
        addLibrary(navController, RootScreen.Library)
        addAccount(RootScreen.Library, openSettings)
        addShowDetails(navController, RootScreen.Library)
        addShowSeasons(navController, RootScreen.Library)
        addEpisodeDetails(navController, RootScreen.Library)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Search.route,
        startDestination = Screen.Search.createRoute(RootScreen.Search),
    ) {
        addSearch(navController, RootScreen.Search)
        addAccount(RootScreen.Search, openSettings)
        addShowDetails(navController, RootScreen.Search)
        addShowSeasons(navController, RootScreen.Search)
        addEpisodeDetails(navController, RootScreen.Search)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscover(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.Discover.createRoute(root),
        debugLabel = "Discover()",
    ) {
        Discover(
            openTrendingShows = {
                navController.navigate(Screen.Trending.createRoute(root))
            },
            openPopularShows = {
                navController.navigate(Screen.Popular.createRoute(root))
            },
            openRecommendedShows = {
                navController.navigate(Screen.RecommendedShows.createRoute(root))
            },
            openShowDetails = { showId, seasonId, episodeId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))

                // If we have an season id, we also open that
                if (seasonId != null) {
                    navController.navigate(
                        Screen.ShowSeasons.createRoute(root, showId, seasonId),
                    )
                }
                // If we have an episodeId, we also open that
                if (episodeId != null) {
                    navController.navigate(Screen.EpisodeDetails.createRoute(root, episodeId))
                }
            },
            openUser = {
                navController.navigate(Screen.Account.createRoute(root))
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addLibrary(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.Library.createRoute(root),
        debugLabel = "Library()",
    ) {
        Library(
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
            openUser = {
                navController.navigate(Screen.Account.createRoute(root))
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearch(
    navController: NavController,
    root: RootScreen,
) {
    composable(Screen.Search.createRoute(root)) {
        Search(
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowDetails(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.ShowDetails.createRoute(root),
        debugLabel = "ShowDetails()",
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType },
        ),
    ) {
        ShowDetails(
            navigateUp = navController::navigateUp,
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
            openEpisodeDetails = { episodeId ->
                navController.navigate(Screen.EpisodeDetails.createRoute(root, episodeId))
            },
            openSeasons = { showId, seasonId ->
                navController.navigate(Screen.ShowSeasons.createRoute(root, showId, seasonId))
            },
        )
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@ExperimentalAnimationApi
private fun NavGraphBuilder.addEpisodeDetails(
    navController: NavController,
    root: RootScreen,
) {
    bottomSheet(
        route = Screen.EpisodeDetails.createRoute(root),
        debugLabel = "EpisodeDetails()",
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        ),
    ) {
        val bottomSheetNavigator = navController.navigatorProvider
            .getNavigator(BottomSheetNavigator::class.java)
        EpisodeDetails(
            expandedValue = bottomSheetNavigator.navigatorSheetState.currentValue,
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addRecommendedShows(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.RecommendedShows.createRoute(root),
        debugLabel = "RecommendedShows()",
    ) {
        RecommendedShows(
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addTrendingShows(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.Trending.createRoute(root),
        debugLabel = "TrendingShows()",
    ) {
        TrendingShows(
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addPopularShows(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.Popular.createRoute(root),
        debugLabel = "PopularShows()",
    ) {
        PopularShows(
            openShowDetails = { showId ->
                navController.navigate(Screen.ShowDetails.createRoute(root, showId))
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addAccount(
    root: RootScreen,
    onOpenSettings: () -> Unit,
) {
    dialog(
        route = Screen.Account.createRoute(root),
        debugLabel = "AccountUi()",
    ) {
        AccountUi(
            openSettings = onOpenSettings,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowSeasons(
    navController: NavController,
    root: RootScreen,
) {
    composable(
        route = Screen.ShowSeasons.createRoute(root),
        debugLabel = "ShowSeasons()",
        arguments = listOf(
            navArgument("showId") {
                type = NavType.LongType
            },
            navArgument("seasonId") {
                type = NavType.StringType
                nullable = true
            },
        ),
    ) {
        ShowSeasons(
            navigateUp = navController::navigateUp,
            openEpisodeDetails = { episodeId ->
                navController.navigate(Screen.EpisodeDetails.createRoute(root, episodeId))
            },
            initialSeasonId = it.arguments?.getString("seasonId")?.toLong(),
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
