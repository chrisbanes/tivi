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

package app.tivi.home.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import app.tivi.Screen
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.Carousel
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.RefreshButton
import app.tivi.common.compose.Scaffold
import app.tivi.common.compose.UserProfileButton
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun Discover(navController: NavController) {
    Discover(
        viewModel = hiltNavGraphViewModel(),
        navController = navController,
    )
}

@Composable
internal fun Discover(
    viewModel: DiscoverViewModel,
    navController: NavController,
) {
    val viewState by viewModel.state.collectAsState()
    Discover(state = viewState) { action ->
        when (action) {
            DiscoverAction.LoginAction,
            DiscoverAction.OpenUserDetails -> navController.navigate(Screen.Account.route)
            is DiscoverAction.OpenShowDetails -> {
                navController.navigate("show/${action.showId}")
                // If we have an episodeId, we also open that
                if (action.episodeId != null) {
                    navController.navigate("episode/${action.episodeId}")
                }
            }
            DiscoverAction.OpenTrendingShows -> navController.navigate(Screen.Trending.route)
            DiscoverAction.OpenPopularShows -> navController.navigate(Screen.Popular.route)
            DiscoverAction.OpenRecommendedShows -> {
                navController.navigate(Screen.RecommendedShows.route)
            }
            else -> viewModel.submitAction(action)
        }
    }
}

@Composable
internal fun Discover(
    state: DiscoverViewState,
    actioner: (DiscoverAction) -> Unit
) {
    Scaffold(
        topBar = {
            DiscoverAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.refreshing,
                onRefreshActionClick = { actioner(DiscoverAction.RefreshAction) },
                onUserActionClick = { actioner(DiscoverAction.OpenUserDetails) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.refreshing),
            onRefresh = { actioner(DiscoverAction.RefreshAction) },
            indicatorPadding = paddingValues,
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true
                )
            }
        ) {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.fillMaxSize(),
            ) {
                itemSpacer(16.dp)

                state.nextEpisodeWithShowToWatched?.let { nextEpisodeToWatch ->
                    item {
                        Header(title = stringResource(R.string.discover_keep_watching_title))
                    }
                    item {
                        NextEpisodeToWatch(
                            show = nextEpisodeToWatch.show,
                            poster = nextEpisodeToWatch.poster,
                            season = nextEpisodeToWatch.season,
                            episode = nextEpisodeToWatch.episode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    actioner(
                                        DiscoverAction.OpenShowDetails(
                                            showId = nextEpisodeToWatch.show.id,
                                            episodeId = nextEpisodeToWatch.episode.id
                                        )
                                    )
                                }
                        )
                    }

                    itemSpacer(16.dp)
                }

                item {
                    CarouselWithHeader(
                        items = state.trendingItems,
                        title = stringResource(R.string.discover_trending_title),
                        refreshing = state.trendingRefreshing,
                        onItemClick = { actioner(DiscoverAction.OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(DiscoverAction.OpenTrendingShows) }
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.recommendedItems,
                        title = stringResource(R.string.discover_recommended_title),
                        refreshing = state.recommendedRefreshing,
                        onItemClick = { actioner(DiscoverAction.OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(DiscoverAction.OpenRecommendedShows) }
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.popularItems,
                        title = stringResource(R.string.discover_popular_title),
                        refreshing = state.popularRefreshing,
                        onItemClick = { actioner(DiscoverAction.OpenShowDetails(it.id)) },
                        onMoreClick = { actioner(DiscoverAction.OpenPopularShows) }
                    )
                }

                itemSpacer(16.dp)
            }
        }
    }
}

@Composable
private fun NextEpisodeToWatch(
    show: TiviShow,
    poster: TmdbImageEntity?,
    season: Season,
    episode: Episode,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            if (poster != null) {
                PosterCard(
                    show = show,
                    poster = poster,
                    modifier = Modifier
                        .width(64.dp)
                        .aspectRatio(2 / 3f)
                )

                Spacer(Modifier.width(16.dp))
            }

            Column(Modifier.align(Alignment.CenterVertically)) {
                val textCreator = LocalTiviTextCreator.current
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.disabled) {
                    Text(
                        text = textCreator.seasonEpisodeTitleText(season, episode),
                        style = MaterialTheme.typography.caption
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = episode.title
                        ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                    style = MaterialTheme.typography.body1
                )
            }
        }
    }
}

@Composable
private fun <T : EntryWithShow<*>> CarouselWithHeader(
    items: List<T>,
    title: String,
    refreshing: Boolean,
    onItemClick: (TiviShow) -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        if (refreshing || items.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))

            Header(
                title = title,
                loading = refreshing,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = onMoreClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colors.secondary
                    ),
                    modifier = Modifier.alignBy(FirstBaseline)
                ) {
                    Text(text = stringResource(R.string.header_more))
                }
            }
        }
        if (items.isNotEmpty()) {
            EntryShowCarousel(
                items = items,
                onItemClick = onItemClick,
                modifier = Modifier
                    .height(192.dp)
                    .fillMaxWidth()
            )
        }
        // TODO empty state
    }
}

@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
    items: List<T>,
    onItemClick: (TiviShow) -> Unit,
    modifier: Modifier = Modifier
) {
    Carousel(
        items = items,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        itemSpacing = 4.dp,
        modifier = modifier
    ) { item, padding ->
        PosterCard(
            show = item.show,
            poster = item.poster,
            onClick = { onItemClick(item.show) },
            modifier = Modifier
                .padding(padding)
                .fillParentMaxHeight()
                .aspectRatio(2 / 3f)
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun Header(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit = {}
) {
    Row(modifier) {
        Spacer(Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(vertical = 8.dp)
        )

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = loading) {
            AutoSizedCircularProgressIndicator(
                color = MaterialTheme.colors.secondary,
                modifier = Modifier
                    .padding(8.dp)
                    .size(16.dp)
            )
        }

        content()

        Spacer(Modifier.width(16.dp))
    }
}

@Composable
private fun DiscoverAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = AppBarAlphas.translucentBarAlpha()),
        contentColor = MaterialTheme.colors.onSurface,
        elevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .height(56.dp)
                .padding(start = 16.dp, end = 4.dp)
        ) {
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                // This button refresh allows screen-readers, etc to trigger a refresh.
                // We only show the button to trigger a refresh, not to indicate that
                // we're currently refreshing, otherwise we have 4 indicators showing the
                // same thing.
                Crossfade(
                    targetState = refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    if (!refreshing) {
                        RefreshButton(onClick = onRefreshActionClick)
                    }
                }

                UserProfileButton(
                    loggedIn = loggedIn,
                    user = user,
                    onClick = onUserActionClick,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewDiscoverAppBar() {
    DiscoverAppBar(
        loggedIn = false,
        user = null,
        refreshing = false,
        onUserActionClick = {},
        onRefreshActionClick = {}
    )
}

@Preview
@Composable
private fun PreviewHeader() {
    Surface(Modifier.fillMaxWidth()) {
        Header(
            title = "Being watched now",
            loading = true
        )
    }
}
