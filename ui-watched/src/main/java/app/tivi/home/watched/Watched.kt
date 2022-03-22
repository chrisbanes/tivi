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

package app.tivi.home.watched

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.itemsInGrid
import app.tivi.common.compose.rememberFlowWithLifecycle
import app.tivi.common.compose.rememberStateWithLifecycle
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.FilterSortPanel
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.SwipeDismissSnackbarHost
import app.tivi.common.compose.ui.UserProfileButton
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.threeten.bp.OffsetDateTime

@Composable
fun Watched(
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) {
    Watched(
        viewModel = hiltViewModel(),
        openShowDetails = openShowDetails,
        openUser = openUser,
    )
}

@Composable
internal fun Watched(
    viewModel: WatchedViewModel,
    openShowDetails: (showId: Long) -> Unit,
    openUser: () -> Unit,
) {
    val viewState by rememberStateWithLifecycle(viewModel.state)
    val pagingItems = rememberFlowWithLifecycle(viewModel.pagedList)
        .collectAsLazyPagingItems()

    Watched(
        state = viewState,
        list = pagingItems,
        openShowDetails = openShowDetails,
        onMessageShown = { viewModel.clearMessage(it) },
        openUser = openUser,
        refresh = { viewModel.refresh() },
        onFilterChanged = { viewModel.setFilter(it) },
        onSortSelected = { viewModel.setSort(it) },
    )
}

@Composable
internal fun Watched(
    state: WatchedViewState,
    list: LazyPagingItems<WatchedShowEntryWithShow>,
    openShowDetails: (showId: Long) -> Unit,
    onMessageShown: (id: Long) -> Unit,
    refresh: () -> Unit,
    openUser: () -> Unit,
    onFilterChanged: (String) -> Unit,
    onSortSelected: (SortOption) -> Unit,
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
            WatchedAppBar(
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
        modifier = Modifier.fillMaxSize(),
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

            LazyColumn(
                contentPadding = paddingValues,
                modifier = Modifier
                    .bodyWidth()
                    .fillMaxHeight()
            ) {
                item {
                    FilterSortPanel(
                        filterHint = stringResource(R.string.filter_shows, list.itemCount),
                        onFilterChanged = onFilterChanged,
                        sortOptions = state.availableSorts,
                        currentSortOption = state.sort,
                        onSortSelected = onSortSelected,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = bodyMargin)
                    )
                }

                itemsInGrid(
                    lazyPagingItems = list,
                    columns = columns / 4,
                    // We minus 8.dp off the grid padding, as we use content padding on the items below
                    contentPadding = PaddingValues(
                        horizontal = (bodyMargin - 8.dp).coerceAtLeast(0.dp),
                        vertical = (gutter - 8.dp).coerceAtLeast(0.dp),
                    ),
                    verticalItemPadding = (gutter - 8.dp).coerceAtLeast(0.dp),
                    horizontalItemPadding = (gutter - 8.dp).coerceAtLeast(0.dp),
                ) { entry ->
                    if (entry != null) {
                        WatchedShowItem(
                            show = entry.show,
                            poster = entry.poster,
                            lastWatched = entry.entry.lastWatched,
                            onClick = { openShowDetails(entry.show.id) },
                            contentPadding = PaddingValues(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    // TODO placeholder?
                }

                itemSpacer(16.dp)
            }
        }
    }
}

@Composable
private fun WatchedShowItem(
    show: TiviShow,
    poster: ShowTmdbImage?,
    lastWatched: OffsetDateTime,
    onClick: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
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
                style = MaterialTheme.typography.subtitle1,
            )

            Spacer(Modifier.height(2.dp))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = stringResource(
                        R.string.library_last_watched,
                        LocalTiviDateFormatter.current.formatShortRelativeTime(lastWatched)
                    ),
                    style = MaterialTheme.typography.caption,
                )
            }
        }
    }
}

@Composable
private fun WatchedAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        title = { Text(text = stringResource(R.string.watched_shows_title)) },
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
