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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltNavGraphViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.navigate
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import app.tivi.Screen
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.RefreshButton
import app.tivi.common.compose.Scaffold
import app.tivi.common.compose.SearchTextField
import app.tivi.common.compose.SortMenuPopup
import app.tivi.common.compose.UserProfileButton
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.WatchedShowEntryWithShow
import app.tivi.trakt.TraktAuthState
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.threeten.bp.OffsetDateTime

@Composable
fun Watched(navController: NavController) {
    Watched(
        viewModel = hiltNavGraphViewModel(),
        navController = navController,
    )
}

@Composable
internal fun Watched(
    viewModel: WatchedViewModel,
    navController: NavController,
) {
    val viewState by viewModel.state.collectAsState()
    val pagingItems = viewModel.pagedList.collectAsLazyPagingItems()

    Watched(state = viewState, list = pagingItems) { action ->
        when (action) {
            WatchedAction.LoginAction,
            WatchedAction.OpenUserDetails -> {
                navController.navigate(Screen.Account.route)
            }
            is WatchedAction.OpenShowDetails -> {
                navController.navigate("show/${action.showId}")
            }
            else -> viewModel.submitAction(action)
        }
    }
}

@Composable
internal fun Watched(
    state: WatchedViewState,
    list: LazyPagingItems<WatchedShowEntryWithShow>,
    actioner: (WatchedAction) -> Unit
) {
    Scaffold(
        topBar = {
            WatchedAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.isLoading,
                onRefreshActionClick = { actioner(WatchedAction.RefreshAction) },
                onUserActionClick = { actioner(WatchedAction.OpenUserDetails) },
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(state.isLoading),
            onRefresh = { actioner(WatchedAction.RefreshAction) },
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
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    FilterSortPanel(
                        filterHint = stringResource(R.string.filter_shows, list.itemCount),
                        onFilterChanged = { actioner(WatchedAction.FilterShows(it)) },
                        sortOptions = state.availableSorts,
                        currentSortOption = state.sort,
                        onSortSelected = { actioner(WatchedAction.ChangeSort(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(list) { entry ->
                    if (entry != null) {
                        WatchedShowItem(
                            show = entry.show,
                            poster = entry.poster,
                            lastWatched = entry.entry.lastWatched,
                            onClick = { actioner(WatchedAction.OpenShowDetails(entry.show.id)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(88.dp)
                        )
                    } else {
                        // TODO placeholder?
                    }
                }

                itemSpacer(16.dp)
            }
        }
    }
}

@Composable
private fun FilterSortPanel(
    filterHint: String,
    onFilterChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    sortOptions: List<SortOption>,
    currentSortOption: SortOption,
    onSortSelected: (SortOption) -> Unit,
) {
    Row(modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        var filter by remember { mutableStateOf(TextFieldValue()) }

        SearchTextField(
            value = filter,
            onValueChange = { value ->
                filter = value
                onFilterChanged(value.text)
            },
            hint = filterHint,
            modifier = Modifier.weight(1f)
        )

        SortMenuPopup(
            sortOptions = sortOptions,
            currentSortOption = currentSortOption,
            onSortSelected = onSortSelected,
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_sort_black_24dp),
                contentDescription = stringResource(R.string.cd_sort_list),
            )
        }
    }
}

@Composable
private fun WatchedShowItem(
    show: TiviShow,
    poster: ShowTmdbImage?,
    lastWatched: OffsetDateTime,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textCreator = LocalTiviTextCreator.current
    Row(
        modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Spacer(Modifier.width(16.dp))

        if (poster != null) {
            Surface(
                elevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .fillMaxHeight()
                    .aspectRatio(2 / 3f)
            ) {
                Image(
                    painter = rememberCoilPainter(poster, fadeIn = true),
                    contentDescription = stringResource(
                        R.string.cd_show_poster_image,
                        show.title
                            ?: ""
                    ),
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(end = 16.dp)
            ) {
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

                Spacer(Modifier.weight(1f))

                Spacer(Modifier.height(8.dp))
            }

            Divider()
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
                text = stringResource(R.string.watched_shows_title),
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
                ) { isRefreshing ->
                    if (!isRefreshing) {
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
