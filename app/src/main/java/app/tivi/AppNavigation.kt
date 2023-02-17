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

@file:Suppress("UNUSED_PARAMETER")

package app.tivi

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import app.tivi.common.compose.ui.androidMinWidthDialogSize
import app.tivi.home.library.Library
import app.tivi.home.recommended.RecommendedShows
import app.tivi.home.search.Search
import app.tivi.home.upnext.UpNext
import app.tivi.showdetails.details.ShowDetails
import app.tivi.showdetails.seasons.ShowSeasons
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.navigation
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi

internal sealed class RootScreen(val route: String) {
    object Discover : RootScreen("discover")
    object Library : RootScreen("library")
    object UpNext : RootScreen("upnext")
    object Search : RootScreen("search")
}

private sealed class Screen(
    private val route: String,
) {
    fun createRoute(root: RootScreen) = "${root.route}/$route"

    object Discover : Screen("discover")
    object Trending : Screen("trending")
    object Library : Screen("library")
    object UpNext : Screen("upnext")
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

    object EpisodeTrack : Screen("episode/{episodeId}/track") {
        fun createRoute(root: RootScreen, episodeId: Long): String {
            return "${root.route}/episode/$episodeId/track"
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
    composeScreens: ComposeScreens,
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
        addDiscoverTopLevel(navController, composeScreens, onOpenSettings)
        addLibraryTopLevel(navController, composeScreens, onOpenSettings)
        addUpNextTopLevel(navController, composeScreens, onOpenSettings)
        addSearchTopLevel(navController, composeScreens, onOpenSettings)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscoverTopLevel(
    navController: NavController,
    composeScreens: ComposeScreens,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Discover.route,
        startDestination = Screen.Discover.createRoute(RootScreen.Discover),
    ) {
        addDiscover(navController, composeScreens, RootScreen.Discover)
        addAccount(RootScreen.Discover, composeScreens, openSettings)
        addShowDetails(navController, composeScreens, RootScreen.Discover)
        addShowSeasons(navController, composeScreens, RootScreen.Discover)
        addEpisodeDetails(navController, composeScreens, RootScreen.Discover)
        addEpisodeTrack(navController, composeScreens, RootScreen.Discover)
        addRecommendedShows(navController, composeScreens, RootScreen.Discover)
        addTrendingShows(navController, composeScreens, RootScreen.Discover)
        addPopularShows(navController, composeScreens, RootScreen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addLibraryTopLevel(
    navController: NavController,
    composeScreens: ComposeScreens,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Library.route,
        startDestination = Screen.Library.createRoute(RootScreen.Library),
    ) {
        addLibrary(navController, composeScreens, RootScreen.Library)
        addAccount(RootScreen.Library, composeScreens, openSettings)
        addShowDetails(navController, composeScreens, RootScreen.Library)
        addShowSeasons(navController, composeScreens, RootScreen.Library)
        addEpisodeDetails(navController, composeScreens, RootScreen.Library)
        addEpisodeTrack(navController, composeScreens, RootScreen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addUpNextTopLevel(
    navController: NavController,
    composeScreens: ComposeScreens,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.UpNext.route,
        startDestination = Screen.UpNext.createRoute(RootScreen.UpNext),
    ) {
        addUpNext(navController, composeScreens, RootScreen.UpNext)
        addAccount(RootScreen.UpNext, composeScreens, openSettings)
        addShowDetails(navController, composeScreens, RootScreen.UpNext)
        addShowSeasons(navController, composeScreens, RootScreen.UpNext)
        addEpisodeDetails(navController, composeScreens, RootScreen.UpNext)
        addEpisodeTrack(navController, composeScreens, RootScreen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addSearchTopLevel(
    navController: NavController,
    composeScreens: ComposeScreens,
    openSettings: () -> Unit,
) {
    navigation(
        route = RootScreen.Search.route,
        startDestination = Screen.Search.createRoute(RootScreen.Search),
    ) {
        addSearch(navController, composeScreens, RootScreen.Search)
        addAccount(RootScreen.Search, composeScreens, openSettings)
        addShowDetails(navController, composeScreens, RootScreen.Search)
        addShowSeasons(navController, composeScreens, RootScreen.Search)
        addEpisodeDetails(navController, composeScreens, RootScreen.Search)
        addEpisodeTrack(navController, composeScreens, RootScreen.Discover)
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addDiscover(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.Discover.createRoute(root),
        debugLabel = "Discover()",
    ) {
        composeScreens.discover(
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
                navController.navigateToShow(root, showId, seasonId, episodeId)
            },
            openUser = {
                navController.navigate(Screen.Account.createRoute(root))
            },
        )
    }
}

private fun NavController.navigateToShow(
    root: RootScreen,
    showId: Long,
    seasonId: Long? = null,
    episodeId: Long? = null,
) {
    navigate(Screen.ShowDetails.createRoute(root, showId))
    // If we have an season id, we also open that
    if (seasonId != null) {
        navigate(Screen.ShowSeasons.createRoute(root, showId, seasonId))
    }
    // If we have an episodeId, we also open that
    if (episodeId != null) {
        navigate(Screen.EpisodeDetails.createRoute(root, episodeId))
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addLibrary(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.Library.createRoute(root),
        debugLabel = "Library()",
    ) {
        composeScreens.library(
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
            },
            openUser = {
                navController.navigate(Screen.Account.createRoute(root))
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addUpNext(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.UpNext.createRoute(root),
        debugLabel = "UpNext()",
    ) {
        composeScreens.upNext(
            openShowDetails = { showId, seasonId, episodeId ->
                navController.navigateToShow(root, showId, seasonId, episodeId)
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
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(Screen.Search.createRoute(root)) {
        composeScreens.search(
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
            },
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowDetails(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.ShowDetails.createRoute(root),
        debugLabel = "ShowDetails()",
        arguments = listOf(
            navArgument("showId") { type = NavType.LongType },
        ),
    ) {
        composeScreens.showDetails(
            navigateUp = navController::navigateUp,
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
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

@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalAnimationApi
private fun NavGraphBuilder.addEpisodeDetails(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    bottomSheet(
        route = Screen.EpisodeDetails.createRoute(root),
        debugLabel = "EpisodeDetails()",
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        ),
    ) { backStackEntry ->
        val episodeId = backStackEntry.arguments!!.getLong("episodeId")

        composeScreens.episodeDetails(
            sheetState = navController.navigatorProvider
                .getNavigator(BottomSheetNavigator::class.java)
                .navigatorSheetState,
            navigateUp = navController::navigateUp,
            navigateToTrack = {
                navController.navigate(
                    Screen.EpisodeTrack.createRoute(root, episodeId)
                )
            },
        )
    }
}


@OptIn(ExperimentalMaterialNavigationApi::class)
@ExperimentalAnimationApi
private fun NavGraphBuilder.addEpisodeTrack(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    bottomSheet(
        route = Screen.EpisodeTrack.createRoute(root),
        debugLabel = "EpisodeTrack()",
        arguments = listOf(
            navArgument("episodeId") { type = NavType.LongType },
        ),
    ) {
        composeScreens.episodeTrack(
            sheetState = navController.navigatorProvider
                .getNavigator(BottomSheetNavigator::class.java)
                .navigatorSheetState,
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addRecommendedShows(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.RecommendedShows.createRoute(root),
        debugLabel = "RecommendedShows()",
    ) {
        composeScreens.recommendedShows(
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addTrendingShows(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.Trending.createRoute(root),
        debugLabel = "TrendingShows()",
    ) {
        composeScreens.trendingShows(
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
            },
            navigateUp = navController::navigateUp,
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addPopularShows(
    navController: NavController,
    composeScreens: ComposeScreens,
    root: RootScreen,
) {
    composable(
        route = Screen.Popular.createRoute(root),
        debugLabel = "PopularShows()",
    ) {
        composeScreens.popularShows(
            openShowDetails = { showId ->
                navController.navigateToShow(root, showId)
            },
            navigateUp = navController::navigateUp,
        )
    }
}

private fun NavGraphBuilder.addAccount(
    root: RootScreen,
    composeScreens: ComposeScreens,
    onOpenSettings: () -> Unit,
) {
    dialog(
        route = Screen.Account.createRoute(root),
        debugLabel = "AccountUi()",
        // Required due to https://issuetracker.google.com/issues/221643630
        dialogProperties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        composeScreens.accountUi(
            openSettings = onOpenSettings,
            modifier = Modifier
                // Required due to `usePlatformDefaultWidth = false` above
                .androidMinWidthDialogSize(clampMaxWidth = true),
        )
    }
}

@ExperimentalAnimationApi
private fun NavGraphBuilder.addShowSeasons(
    navController: NavController,
    composeScreens: ComposeScreens,
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
        composeScreens.showSeasons(
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
