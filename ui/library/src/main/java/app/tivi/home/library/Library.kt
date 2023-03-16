/*
 * Copyright 2022 Google LLC
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

package app.tivi.home.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.items
import app.tivi.common.compose.rememberLazyGridState
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.SearchTextField
import app.tivi.common.compose.ui.SortChip
import app.tivi.common.compose.ui.TiviStandardAppBar
import app.tivi.common.compose.ui.plus
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

typealias Library = @Composable (
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) -> Unit

@Inject
@Composable
fun Library(
    viewModelFactory: () -> LibraryViewModel,
    @Assisted openShowDetails: (showId: Long) -> Unit,
    @Assisted openUser: () -> Unit,
) {
    Library(
        viewModel = viewModel(factory = viewModelFactory),
        openShowDetails = openShowDetails,
        openUser = openUser,
    )
}

@Composable
internal fun Library(
    viewModel: LibraryViewModel,
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    val pagingItems = viewModel.pagedList.collectAsLazyPagingItems()

    Library(
        state = viewState,
        lazyPagingItems = pagingItems,
        openShowDetails = openShowDetails,
        onMessageShown = viewModel::clearMessage,
        onToggleIncludeFollowedShows = viewModel::toggleFollowedShowsIncluded,
        onToggleIncludeWatchedShows = viewModel::toggleWatchedShowsIncluded,
        openUser = openUser,
        refresh = viewModel::refresh,
        onFilterChanged = viewModel::setFilter,
        onSortSelected = viewModel::setSort,
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun Library(
    state: LibraryViewState,
    lazyPagingItems: LazyPagingItems<LibraryShow>,
    openShowDetails: (showId: Long) -> Unit,
    onMessageShown: (id: Long) -> Unit,
    onToggleIncludeFollowedShows: () -> Unit,
    onToggleIncludeWatchedShows: () -> Unit,
    refresh: () -> Unit,
    openUser: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit,
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
                title = stringResource(UiR.string.library_title),
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

            var filterExpanded by remember { mutableStateOf(false) }

            LazyVerticalGrid(
                state = rememberLazyGridState(lazyPagingItems.itemCount == 0),
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
                    var filter by rememberSaveable(stateSaver = TextFieldValue.Saver) {
                        mutableStateOf(TextFieldValue(state.filter ?: ""))
                    }

                    FilterSortPanel(
                        filterIcon = {
                            IconButton(onClick = { filterExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null, // FIXME
                                )
                            }
                        },
                        filterTextField = {
                            SearchTextField(
                                value = filter,
                                onValueChange = { value ->
                                    filter = value
                                    onFilterChanged(value.text)
                                },
                                hint = stringResource(UiR.string.filter_shows, lazyPagingItems.itemCount),
                                modifier = Modifier.fillMaxWidth(),
                                showClearButton = true,
                                onCleared = {
                                    filter = TextFieldValue()
                                    onFilterChanged("")
                                    filterExpanded = false
                                },
                            )
                        },
                        filterExpanded = filterExpanded,
                        modifier = Modifier.padding(vertical = 8.dp),
                    ) {
                        FilterChip(
                            selected = state.followedShowsIncluded,
                            leadingIcon = {
                                AnimatedVisibility(visible = state.followedShowsIncluded) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                    )
                                }
                            },
                            onClick = onToggleIncludeFollowedShows,
                            label = {
                                Text(text = stringResource(UiR.string.following_shows_title))
                            },
                        )

                        FilterChip(
                            selected = state.watchedShowsIncluded,
                            leadingIcon = {
                                AnimatedVisibility(visible = state.watchedShowsIncluded) {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = null,
                                    )
                                }
                            },
                            onClick = onToggleIncludeWatchedShows,
                            label = {
                                Text(text = stringResource(UiR.string.watched_shows_title))
                            },
                        )

                        SortChip(
                            sortOptions = state.availableSorts,
                            currentSortOption = state.sort,
                            onSortSelected = onSortSelected,
                        )
                    }
                }

                items(
                    items = lazyPagingItems,
                    key = { it.show.id },
                ) { entry ->
                    if (entry != null) {
                        LibraryItem(
                            show = entry.show,
                            watchedEpisodeCount = entry.stats?.watchedEpisodeCount,
                            totalEpisodeCount = entry.stats?.episodeCount,
                            lastWatchedDate = entry.watchedEntry?.lastWatched,
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
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues),
                scale = true,
            )
        }
    }
}

@Composable
private fun FilterSortPanel(
    filterExpanded: Boolean,
    filterIcon: @Composable () -> Unit,
    filterTextField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AnimatedVisibility(!filterExpanded) {
                filterIcon()
            }

            content()
        }

        AnimatedVisibility(visible = filterExpanded) {
            filterTextField()
        }
    }
}

@Composable
private fun LibraryItem(
    show: TiviShow,
    watchedEpisodeCount: Int?,
    totalEpisodeCount: Int?,
    lastWatchedDate: Instant?,
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

            if (watchedEpisodeCount != null && totalEpisodeCount != null) {
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
            } else if (lastWatchedDate != null) {
                Text(
                    text = stringResource(
                        UiR.string.library_last_watched,
                        LocalTiviDateFormatter.current.formatShortRelativeTime(lastWatchedDate),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}
