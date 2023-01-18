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

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.home.discover

import androidx.activity.compose.ReportDrawnWhen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.TiviStandardAppBar
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.trakt.TraktAuthState

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
    val viewState by viewModel.state.collectAsState()

    Discover(
        state = viewState,
        refresh = viewModel::refresh,
        openUser = openUser,
        openShowDetails = openShowDetails,
        openTrendingShows = openTrendingShows,
        openRecommendedShows = openRecommendedShows,
        openPopularShows = openPopularShows,
        onMessageShown = viewModel::clearMessage,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Discover(
    state: DiscoverViewState,
    refresh: () -> Unit,
    openUser: () -> Unit,
    openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
    openTrendingShows: () -> Unit,
    openRecommendedShows: () -> Unit,
    openPopularShows: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val dismissSnackbarState = rememberDismissState { value ->
        when {
            value != DismissValue.Default -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            }
            else -> false
        }
    }

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    ReportDrawnWhen {
        !state.popularRefreshing &&
            !state.trendingRefreshing &&
            state.popularItems.isNotEmpty() &&
            state.trendingItems.isNotEmpty()
    }

    Scaffold(
        topBar = {
            TiviStandardAppBar(
                title = stringResource(UiR.string.discover_title),
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.refreshing,
                onRefreshActionClick = refresh,
                onUserActionClick = openUser,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SwipeToDismiss(
                    state = dismissSnackbarState,
                    background = {},
                    dismissContent = { Snackbar(snackbarData = data) },
                    modifier = Modifier
                        .padding(horizontal = Layout.bodyMargin)
                        .fillMaxWidth(),
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        val refreshState = rememberPullRefreshState(refreshing = false, onRefresh = refresh)
        Box(modifier = Modifier.pullRefresh(state = refreshState)) {
            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier.bodyWidth(),
            ) {
                item {
                    Spacer(Modifier.height(Layout.gutter))
                }

                state.nextEpisodeWithShowToWatched?.let { nextEpisodeToWatch ->
                    item {
                        NextEpisodeToWatch(
                            show = nextEpisodeToWatch.show,
                            poster = nextEpisodeToWatch.poster,
                            season = nextEpisodeToWatch.season,
                            episode = nextEpisodeToWatch.episode,
                            onClick = {
                                openShowDetails(
                                    nextEpisodeToWatch.show.id,
                                    nextEpisodeToWatch.episode.seasonId,
                                    nextEpisodeToWatch.episode.id,
                                )
                            },
                            modifier = Modifier
                                .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
                                .fillMaxWidth(),
                        )
                    }

                    item {
                        Spacer(Modifier.height(Layout.gutter))
                    }
                }

                item {
                    CarouselWithHeader(
                        items = state.trendingItems,
                        title = stringResource(UiR.string.discover_trending_title),
                        refreshing = state.trendingRefreshing,
                        onItemClick = {
                            openShowDetails(it.id, null, null)
                        },
                        onMoreClick = openTrendingShows,
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.recommendedItems,
                        title = stringResource(UiR.string.discover_recommended_title),
                        refreshing = state.recommendedRefreshing,
                        onItemClick = {
                            openShowDetails(it.id, null, null)
                        },
                        onMoreClick = openRecommendedShows,
                    )
                }

                item {
                    CarouselWithHeader(
                        items = state.popularItems,
                        title = stringResource(UiR.string.discover_popular_title),
                        refreshing = state.popularRefreshing,
                        onItemClick = { openShowDetails(it.id, null, null) },
                        onMoreClick = openPopularShows,
                    )
                }

                item {
                    Spacer(Modifier.height(Layout.gutter))
                }
            }

            PullRefreshIndicator(
                refreshing = state.refreshing,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter).padding(paddingValues),
                scale = true,
            )
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
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier,
    ) {
        Column(Modifier.padding(Layout.bodyMargin)) {
            Header(
                title = stringResource(UiR.string.discover_keep_watching_title),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(Modifier.fillMaxWidth()) {
                if (poster != null) {
                    PosterCard(
                        show = show,
                        poster = poster,
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .width(64.dp)
                            .aspectRatio(2 / 3f),
                    )
                }

                Column(Modifier.align(Alignment.CenterVertically)) {
                    val textCreator = LocalTiviTextCreator.current
                    Text(
                        text = textCreator.seasonEpisodeTitleText(season, episode),
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = episode.title
                            ?: stringResource(UiR.string.episode_title_fallback, episode.number!!),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
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
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .testTag("discover_carousel"),
    ) {
        if (refreshing || items.isNotEmpty()) {
            Spacer(Modifier.height(Layout.gutter))

            Header(
                title = title,
                loading = refreshing,
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth(),
            ) {
                TextButton(
                    onClick = onMoreClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary,
                    ),
                    modifier = Modifier.alignBy(FirstBaseline),
                ) {
                    Text(text = stringResource(UiR.string.header_more))
                }
            }
        }
        if (items.isNotEmpty()) {
            EntryShowCarousel(
                items = items,
                onItemClick = onItemClick,
                modifier = Modifier
                    .height(192.dp)
                    .fillMaxWidth(),
            )
        }
        // TODO empty state
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
    items: List<T>,
    onItemClick: (TiviShow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val contentPadding = PaddingValues(horizontal = Layout.bodyMargin, vertical = Layout.gutter)

    LazyRow(
        state = lazyListState,
        modifier = modifier,
        flingBehavior = rememberSnapFlingBehavior(
            snapLayoutInfoProvider = remember(lazyListState) {
                SnapLayoutInfoProvider(
                    lazyListState = lazyListState,
                    positionInLayout = { _, _ -> 0f }, // start
                )
            },
        ),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(
            items = items,
            key = { it.show.id },
        ) { item ->
            PosterCard(
                show = item.show,
                poster = item.poster,
                onClick = { onItemClick(item.show) },
                modifier = Modifier
                    .testTag("discover_carousel_item")
                    .animateItemPlacement()
                    .fillParentMaxHeight()
                    .aspectRatio(2 / 3f),
            )
        }
    }
}

@Composable
private fun Header(
    title: String,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.weight(1f))

        AnimatedVisibility(visible = loading) {
            AutoSizedCircularProgressIndicator(
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(16.dp),
            )
        }

        content()
    }
}

@Preview
@Composable
private fun PreviewHeader() {
    Surface(Modifier.fillMaxWidth()) {
        Header(
            title = "Being watched now",
            loading = true,
        )
    }
}
