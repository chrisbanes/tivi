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

package app.tivi.home.followed

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.items
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.FilterSortPanel
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.SwipeDismissSnackbarHost
import app.tivi.common.compose.ui.UserProfileButton
import app.tivi.common.compose.ui.plus
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import app.tivi.common.ui.resources.R as UiR

@Composable
fun Followed(
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit
) {
    Followed(
        viewModel = hiltViewModel(),
        openShowDetails = openShowDetails,
        openUser = openUser
    )
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun Followed(
    viewModel: FollowedViewModel,
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit
) {
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    val pagingItems = viewModel.pagedList.collectAsLazyPagingItems()

    Followed(
        state = viewState,
        list = pagingItems,
        openShowDetails = openShowDetails,
        onMessageShown = viewModel::clearMessage,
        openUser = openUser,
        refresh = viewModel::refresh,
        onFilterChanged = viewModel::setFilter,
        onSortSelected = viewModel::setSort
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Followed(
    state: FollowedViewState,
    list: LazyPagingItems<FollowedShowEntryWithShow>,
    openShowDetails: (showId: Long) -> Unit,
    onMessageShown: (id: Long) -> Unit,
    refresh: () -> Unit,
    openUser: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit
) {
    val scaffoldState = rememberScaffoldState()

    state.message?.let { message ->
        LaunchedEffect(message) {
            scaffoldState.snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            FollowedAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.isLoading,
                onRefreshActionClick = refresh,
                onUserActionClick = openUser,
                modifier = Modifier.fillMaxWidth()
            )
        },
        snackbarHost = { snackbarHostState ->
            SwipeDismissSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isLoading),
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
            val columns = Layout.columns
            val bodyMargin = Layout.bodyMargin
            val gutter = Layout.gutter

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns / 4),
                contentPadding = paddingValues + PaddingValues(
                    horizontal = (bodyMargin - 8.dp).coerceAtLeast(0.dp),
                    vertical = (gutter - 8.dp).coerceAtLeast(0.dp)
                ),
                // We minus 8.dp off the grid padding, as we use content padding on the items below
                horizontalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
                verticalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
                modifier = Modifier
                    .bodyWidth()
                    .fillMaxHeight()
            ) {
                fullSpanItem {
                    FilterSortPanel(
                        filterHint = stringResource(UiR.string.filter_shows, list.itemCount),
                        onFilterChanged = onFilterChanged,
                        sortOptions = state.availableSorts,
                        currentSortOption = state.sort,
                        onSortSelected = onSortSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }

                items(
                    items = list,
                    key = { it.show.id }
                ) { entry ->
                    if (entry != null) {
                        FollowedShowItem(
                            show = entry.show,
                            poster = entry.poster,
                            watchedEpisodeCount = entry.stats?.watchedEpisodeCount ?: 0,
                            totalEpisodeCount = entry.stats?.episodeCount ?: 0,
                            onClick = { openShowDetails(entry.show.id) },
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier
                                .animateItemPlacement()
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FollowedShowItem(
    show: TiviShow,
    poster: ShowTmdbImage?,
    watchedEpisodeCount: Int,
    totalEpisodeCount: Int,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val textCreator = LocalTiviTextCreator.current
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(contentPadding)
    ) {
        PosterCard(
            show = show,
            poster = poster,
            modifier = Modifier
                .fillMaxWidth(0.2f) // 20% of the width
                .aspectRatio(2 / 3f)
        )

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = textCreator.showTitle(show = show).toString(),
                style = MaterialTheme.typography.subtitle1
            )

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = when {
                    totalEpisodeCount > 0 -> watchedEpisodeCount / totalEpisodeCount.toFloat()
                    else -> 0f
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = textCreator.followedShowEpisodeWatchStatus(
                        episodeCount = totalEpisodeCount,
                        watchedEpisodeCount = watchedEpisodeCount
                    ).toString(),
                    style = MaterialTheme.typography.caption
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun FollowedAppBar(
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
        contentPadding = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .asPaddingValues(),
        modifier = modifier,
        title = { Text(text = stringResource(UiR.string.following_shows_title)) },
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
        }
    )
}
