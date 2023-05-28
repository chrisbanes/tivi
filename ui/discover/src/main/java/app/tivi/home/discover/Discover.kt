/*
 * Copyright 2023 Google LLC
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

import android.os.Build
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.TiviStandardAppBar
import app.tivi.common.ui.resources.MR
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.overlays.showInDialog
import app.tivi.screens.AccountScreen
import app.tivi.screens.DiscoverScreen
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class DiscoverUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
        is DiscoverScreen -> {
            ui<DiscoverUiState> { state, modifier ->
                Discover(state, modifier)
            }
        }

        else -> null
    }
}

@Composable
internal fun Discover(
    state: DiscoverUiState,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val overlayHost = LocalOverlayHost.current

    // Need to extract the eventSink out to a local val, so that the Compose Compiler
    // treats it as stable. See: https://issuetracker.google.com/issues/256100927
    val eventSink = state.eventSink

    Discover(
        state = state,
        refresh = { eventSink(DiscoverUiEvent.Refresh(true)) },
        openUser = {
            scope.launch {
                overlayHost.showInDialog(AccountScreen)
            }
        },
        openShowDetails = { showId, seasonId, episodeId ->
            eventSink(DiscoverUiEvent.OpenShowDetails(showId, seasonId, episodeId))
        },
        openTrendingShows = { eventSink(DiscoverUiEvent.OpenTrendingShows) },
        openRecommendedShows = { eventSink(DiscoverUiEvent.OpenRecommendedShows) },
        openPopularShows = { eventSink(DiscoverUiEvent.OpenPopularShows) },
        onMessageShown = { eventSink(DiscoverUiEvent.ClearMessage(it)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun Discover(
    state: DiscoverUiState,
    refresh: () -> Unit,
    openUser: () -> Unit,
    openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
    openTrendingShows: () -> Unit,
    openRecommendedShows: () -> Unit,
    openPopularShows: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val dismissSnackbarState = rememberDismissState(
        confirmValueChange = { value ->
            if (value != DismissValue.Default) {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            } else {
                false
            }
        },
    )

    state.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    if (Build.VERSION.SDK_INT >= 25) {
        // ReportDrawnWhen routinely causes crashes on API < 25:
        // https://issuetracker.google.com/issues/260506820
        ReportDrawnWhen {
            !state.popularRefreshing &&
                !state.trendingRefreshing &&
                state.popularItems.isNotEmpty() &&
                state.trendingItems.isNotEmpty()
        }
    }

    Scaffold(
        topBar = {
            TiviStandardAppBar(
                title = stringResource(MR.strings.discover_title),
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
        modifier = modifier,
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

                state.nextEpisodeWithShowToWatch?.let { nextEpisodeToWatch ->
                    item {
                        NextEpisodeToWatch(
                            show = nextEpisodeToWatch.show,
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
                        title = stringResource(MR.strings.discover_trending_title),
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
                        title = stringResource(MR.strings.discover_recommended_title),
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
                        title = stringResource(MR.strings.discover_popular_title),
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues),
                scale = true,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NextEpisodeToWatch(
    show: TiviShow,
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
                title = stringResource(MR.strings.discover_keep_watching_title),
                modifier = Modifier.padding(bottom = 16.dp),
            )

            Row(Modifier.fillMaxWidth()) {
                PosterCard(
                    show = show,
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .width(64.dp)
                        .aspectRatio(2 / 3f),
                )

                Column(Modifier.align(Alignment.CenterVertically)) {
                    val textCreator = LocalTiviTextCreator.current
                    Text(
                        text = textCreator.seasonEpisodeTitleText(season, episode),
                        style = MaterialTheme.typography.labelMedium,
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = episode.title
                            ?: stringResource(MR.strings.episode_title_fallback, episode.number!!),
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
                    Text(text = stringResource(MR.strings.header_more))
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
