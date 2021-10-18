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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.rememberFlowWithLifecycle
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.UserProfileButton
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior

@Composable
fun Discover(
    openTrendingShows: () -> Unit,
    openPopularShows: () -> Unit,
    openRecommendedShows: () -> Unit,
    openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
    openUser: () -> Unit,
) {
    Discover(
        viewModel = hiltViewModel(),
        openTrendingShows = openTrendingShows,
        openPopularShows = openPopularShows,
        openRecommendedShows = openRecommendedShows,
        openShowDetails = openShowDetails,
        openUser = openUser,
    )
}

@Composable
internal fun Discover(
    viewModel: DiscoverViewModel,
    openTrendingShows: () -> Unit,
    openPopularShows: () -> Unit,
    openRecommendedShows: () -> Unit,
    openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
    openUser: () -> Unit,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = DiscoverViewState.Empty)
    Discover(
        state = viewState,
        refresh = { viewModel.submitAction(DiscoverAction.RefreshAction) },
        openUser = openUser,
        openShowDetails = openShowDetails,
        openTrendingShows = openTrendingShows,
        openRecommendedShows = openRecommendedShows,
        openPopularShows = openPopularShows
    )
}

@Composable
internal fun Discover(
    state: DiscoverViewState,
    refresh: () -> Unit,
    openUser: () -> Unit,
    openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
    openTrendingShows: () -> Unit,
    openRecommendedShows: () -> Unit,
    openPopularShows: () -> Unit,
) {
    Scaffold(
        topBar = {
            DiscoverAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.refreshing,
                onRefreshActionClick = refresh,
                onUserActionClick = openUser,
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.refreshing),
            onRefresh = refresh,
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
                modifier = Modifier.bodyWidth(),
            ) {
                item {
                    Spacer(Modifier.height(Layout.gutter))
                }

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
                                    openShowDetails(
                                        nextEpisodeToWatch.show.id,
                                        nextEpisodeToWatch.episode.seasonId,
                                        nextEpisodeToWatch.episode.id,
                                    )
                                }
                        )
                    }

                    item {
                        Spacer(Modifier.height(Layout.gutter))
                    }
                }

                item {
                    CarouselWithHeader(
                        items = state.trendingItems,
                        title = stringResource(R.string.discover_trending_title),
                        refreshing = state.trendingRefreshing,
                        onItemClick = {
                            openShowDetails(it.id, null, null)
                        },
                        onMoreClick = openTrendingShows
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.recommendedItems,
                        title = stringResource(R.string.discover_recommended_title),
                        refreshing = state.recommendedRefreshing,
                        onItemClick = {
                            openShowDetails(it.id, null, null)
                        },
                        onMoreClick = openRecommendedShows
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.popularItems,
                        title = stringResource(R.string.discover_popular_title),
                        refreshing = state.popularRefreshing,
                        onItemClick = { openShowDetails(it.id, null, null) },
                        onMoreClick = openPopularShows
                    )
                }

                item {
                    Spacer(Modifier.height(Layout.gutter))
                }
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
        Row(
            Modifier.padding(
                horizontal = Layout.bodyMargin,
                vertical = Layout.gutter,
            )
        ) {
            if (poster != null) {
                PosterCard(
                    show = show,
                    poster = poster,
                    modifier = Modifier
                        .width(64.dp)
                        .aspectRatio(2 / 3f)
                )

                Spacer(Modifier.width(Layout.gutter))
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
            Spacer(Modifier.height(Layout.gutter))

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

@OptIn(ExperimentalSnapperApi::class)
@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
    items: List<T>,
    onItemClick: (TiviShow) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val contentPadding = PaddingValues(horizontal = Layout.bodyMargin, vertical = Layout.gutter)

    LazyRow(
        state = lazyListState,
        modifier = modifier,
        flingBehavior = rememberSnapperFlingBehavior(
            lazyListState = lazyListState,
            snapOffsetForItem = SnapOffsets.Start,
            endContentPadding = contentPadding.calculateEndPadding(LayoutDirection.Ltr),
            maximumFlingDistance = {
                // Max fling = 1x scrollable width
                (it.endScrollOffset - it.startScrollOffset).toFloat()
            }
        ),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(items) { item ->
            PosterCard(
                show = item.show,
                poster = item.poster,
                onClick = { onItemClick(item.show) },
                modifier = Modifier
                    .fillParentMaxHeight()
                    .aspectRatio(2 / 3f)
            )
        }
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
        Spacer(Modifier.width(Layout.bodyMargin))

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

        Spacer(Modifier.width(Layout.bodyMargin))
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
    TopAppBar(
        backgroundColor = MaterialTheme.colors.surface.copy(
            alpha = AppBarAlphas.translucentBarAlpha()
        ),
        contentColor = MaterialTheme.colors.onSurface,
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyBottom = false,
        ),
        modifier = modifier,
        title = { Text(text = stringResource(R.string.discover_title)) },
        actions = {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                // This button refresh allows screen-readers, etc to trigger a refresh.
                // We only show the button to trigger a refresh, not to indicate that
                // we're currently refreshing, otherwise we have 4 indicators showing the
                // same thing.
                Crossfade(
                    targetState = refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) { isRefreshing ->
                    if (!isRefreshing) {
                        RefreshButton(onClick = onRefreshActionClick)
                    }
                }
            }

            UserProfileButton(
                loggedIn = loggedIn,
                user = user,
                onClick = onUserActionClick,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        },
    )
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
