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

@file:OptIn(ExperimentalMaterialApi::class)

package app.tivi.home.upnext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.items
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.SortChip
import app.tivi.common.compose.ui.TiviStandardAppBar
import app.tivi.common.compose.ui.plus
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import coil.compose.AsyncImagePainter
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias UpNext = @Composable (
    openShowDetails: (showId: Long, seasonId: Long, episodeId: Long) -> Unit,
    openUser: () -> Unit,
    openTrackEpisode: (episodeId: Long) -> Unit,
) -> Unit

@Inject
@Composable
fun UpNext(
    viewModelFactory: () -> UpNextViewModel,
    @Assisted openShowDetails: (showId: Long, seasonId: Long, episodeId: Long) -> Unit,
    @Assisted openUser: () -> Unit,
    @Assisted openTrackEpisode: (episodeId: Long) -> Unit,
) {
    UpNext(
        viewModel = viewModel(factory = viewModelFactory),
        openShowDetails = openShowDetails,
        openTrackEpisode = openTrackEpisode,
        openUser = openUser,
    )
}

@Composable
internal fun UpNext(
    viewModel: UpNextViewModel,
    openShowDetails: (showId: Long, seasonId: Long, episodeId: Long) -> Unit,
    openUser: () -> Unit,
    openTrackEpisode: (episodeId: Long) -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    val pagingItems = viewModel.pagedList.collectAsLazyPagingItems()

    UpNext(
        state = viewState,
        list = pagingItems,
        openShowDetails = openShowDetails,
        openTrackEpisode = openTrackEpisode,
        onMessageShown = viewModel::clearMessage,
        openUser = openUser,
        refresh = viewModel::refresh,
        onSortSelected = viewModel::setSort,
        onToggleFollowedShowsOnly = viewModel::toggleFollowedShowsOnly,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun UpNext(
    state: UpNextViewState,
    list: LazyPagingItems<UpNextEntry>,
    openShowDetails: (showId: Long, seasonId: Long, episodeId: Long) -> Unit,
    openTrackEpisode: (episodeId: Long) -> Unit,
    onMessageShown: (id: Long) -> Unit,
    refresh: () -> Unit,
    openUser: () -> Unit,
    onSortSelected: (SortOption) -> Unit,
    onToggleFollowedShowsOnly: () -> Unit,
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TiviStandardAppBar(
                title = stringResource(UiR.string.upnext_title),
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                scrollBehavior = scrollBehavior,
                refreshing = state.isLoading,
                onRefreshActionClick = refresh,
                onUserActionClick = openUser,
                modifier = Modifier
                    .fillMaxWidth(),
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
        val refreshState = rememberPullRefreshState(
            refreshing = state.isLoading,
            onRefresh = refresh,
        )
        Box(modifier = Modifier.pullRefresh(state = refreshState)) {
            val columns = Layout.columns
            val bodyMargin = Layout.bodyMargin
            val gutter = Layout.gutter

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns / 4),
                contentPadding = paddingValues + PaddingValues(
                    horizontal = (bodyMargin - 8.dp).coerceAtLeast(0.dp),
                    vertical = (gutter - 8.dp).coerceAtLeast(0.dp),
                ),
                // We minus 8.dp off the grid padding, as we use content padding on the items below
                horizontalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
                verticalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .bodyWidth()
                    .fillMaxHeight(),
            ) {
                fullSpanItem {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                        modifier = Modifier
                            .padding(vertical = 8.dp, horizontal = 8.dp)
                            .fillMaxWidth(),
                    ) {
                        FilterChip(
                            selected = state.followedShowsOnly,
                            leadingIcon = {
                                AnimatedVisibility(visible = state.followedShowsOnly) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                    )
                                }
                            },
                            onClick = onToggleFollowedShowsOnly,
                            label = {
                                Text(text = stringResource(UiR.string.upnext_filter_followed_shows_only_title))
                            },
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        SortChip(
                            sortOptions = state.availableSorts,
                            currentSortOption = state.sort,
                            onSortSelected = onSortSelected,
                        )
                    }
                }

                items(
                    items = list,
                    key = { it.show.id },
                ) { entry ->
                    if (entry != null) {
                        val trackEpisode = SwipeAction(
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.Visibility,
                                    contentDescription = null, // decorative
                                    modifier = Modifier
                                        .padding(vertical = 16.dp, horizontal = 24.dp),
                                )
                            },
                            background = MaterialTheme.colorScheme.secondary,
                            onSwipe = { openTrackEpisode(entry.episode.id) },
                        )

                        SwipeableActionsBox(
                            endActions = listOf(trackEpisode),
                            swipeThreshold = 80.dp, // icon + padding + 8.dp
                            backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
                            modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxWidth(),
                        ) {
                            UpNextItem(
                                show = entry.show,
                                season = entry.season,
                                episode = entry.episode,
                                onClick = {
                                    openShowDetails(entry.show.id, entry.season.id, entry.episode.id)
                                },
                                contentPadding = PaddingValues(8.dp),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = refreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues),
                scale = true,
            )
        }
    }
}

@Composable
private fun UpNextItem(
    show: TiviShow,
    episode: Episode,
    season: Season,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(contentPadding),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.3f) // 30% of the width
                .aspectRatio(16 / 11f),
        ) {
            var model: Any by remember { mutableStateOf(episode.asImageModel()) }

            AsyncImage(
                model = model,
                requestBuilder = { crossfade(true) },
                onState = { state ->
                    if (state is AsyncImagePainter.State.Error) {
                        if (state.result.request.data is EpisodeImageModel) {
                            // If the episode backdrop request failed, fallback to the show backdrop
                            model = show.asImageModel(ImageType.BACKDROP)
                        }
                    }
                },
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }

        Spacer(Modifier.width(16.dp))

        Column {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.showTitle(show = show).toString(),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = textCreator.seasonEpisodeTitleText(season, episode),
                style = MaterialTheme.typography.bodySmall,
            )

            Text(
                text = episode.title ?: "",
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
