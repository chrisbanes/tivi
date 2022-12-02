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

@file:OptIn(ExperimentalMaterialApi::class)

package app.tivi.home.followed

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
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.items
import app.tivi.common.compose.ui.FilterSortPanel
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.TiviStandardAppBar
import app.tivi.common.compose.ui.plus
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.trakt.TraktAuthState

@Composable
fun Followed(
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) {
    Followed(
        viewModel = hiltViewModel(),
        openShowDetails = openShowDetails,
        openUser = openUser,
    )
}

@Composable
internal fun Followed(
    viewModel: FollowedViewModel,
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    val pagingItems = viewModel.pagedList.collectAsLazyPagingItems()

    Followed(
        state = viewState,
        list = pagingItems,
        openShowDetails = openShowDetails,
        onMessageShown = viewModel::clearMessage,
        openUser = openUser,
        refresh = viewModel::refresh,
        onFilterChanged = viewModel::setFilter,
        onSortSelected = viewModel::setSort,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun Followed(
    state: FollowedViewState,
    list: LazyPagingItems<FollowedShowEntryWithShow>,
    openShowDetails: (showId: Long) -> Unit,
    onMessageShown: (id: Long) -> Unit,
    refresh: () -> Unit,
    openUser: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit,
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

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            TiviStandardAppBar(
                title = stringResource(UiR.string.following_shows_title),
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
                    FilterSortPanel(
                        filterHint = stringResource(UiR.string.filter_shows, list.itemCount),
                        onFilterChanged = onFilterChanged,
                        sortOptions = state.availableSorts,
                        currentSortOption = state.sort,
                        onSortSelected = onSortSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    )
                }

                items(
                    items = list,
                    key = { it.show.id },
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
                                .fillMaxWidth(),
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = state.isLoading,
                state = refreshState,
                modifier = Modifier.align(Alignment.TopCenter).padding(paddingValues),
                scale = true,
            )
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
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(contentPadding),
    ) {
        PosterCard(
            show = show,
            poster = poster,
            modifier = Modifier
                .fillMaxWidth(0.2f) // 20% of the width
                .aspectRatio(2 / 3f),
        )

        Spacer(Modifier.width(16.dp))

        Column {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.showTitle(show = show).toString(),
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = when {
                    totalEpisodeCount > 0 -> watchedEpisodeCount / totalEpisodeCount.toFloat()
                    else -> 0f
                },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = textCreator.followedShowEpisodeWatchStatus(
                    episodeCount = totalEpisodeCount,
                    watchedEpisodeCount = watchedEpisodeCount,
                ).toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}
