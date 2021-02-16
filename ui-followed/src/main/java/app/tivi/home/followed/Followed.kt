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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.items
import app.tivi.common.compose.LocalHomeTextCreator
import app.tivi.common.compose.RefreshButton
import app.tivi.common.compose.SearchTextField
import app.tivi.common.compose.SortMenuPopup
import app.tivi.common.compose.UserProfileButton
import app.tivi.common.compose.itemSpacer
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.SortOption
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TraktUser
import app.tivi.data.resultentities.FollowedShowEntryWithShow
import app.tivi.trakt.TraktAuthState
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.statusBarsPadding

@Composable
fun Followed(
    state: FollowedViewState,
    list: LazyPagingItems<FollowedShowEntryWithShow>,
    actioner: (FollowedAction) -> Unit
) {
    Surface(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize()) {
            var appBarHeight by remember { mutableStateOf(0) }

            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    val height = with(LocalDensity.current) { appBarHeight.toDp() }
                    Spacer(Modifier.height(height))
                }

                item {
                    FilterSortPanel(
                        filterHint = stringResource(R.string.filter_shows, list.itemCount),
                        onFilterChanged = { actioner(FollowedAction.FilterShows(it)) },
                        sortOptions = state.availableSorts,
                        currentSortOption = state.sort,
                        onSortSelected = { actioner(FollowedAction.ChangeSort(it)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(list) { entry ->
                    if (entry != null) {
                        FollowedShowItem(
                            show = entry.show,
                            poster = entry.poster,
                            watchedEpisodeCount = entry.stats?.watchedEpisodeCount ?: 0,
                            totalEpisodeCount = entry.stats?.episodeCount ?: 0,
                            onClick = { actioner(FollowedAction.OpenShowDetails(entry.show.id)) },
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

            FollowedAppBar(
                loggedIn = state.authState == TraktAuthState.LOGGED_IN,
                user = state.user,
                refreshing = state.isLoading,
                onRefreshActionClick = { actioner(FollowedAction.RefreshAction) },
                onUserActionClick = { actioner(FollowedAction.OpenUserDetails) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { appBarHeight = it.height }
            )
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
private fun FollowedShowItem(
    show: TiviShow,
    poster: ShowTmdbImage?,
    watchedEpisodeCount: Int,
    totalEpisodeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textCreator = LocalHomeTextCreator.current
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
                CoilImage(
                    data = poster,
                    contentDescription = stringResource(R.string.cd_show_poster_image, show.title ?: ""),
                    fadeIn = true,
                    modifier = Modifier.fillMaxSize()
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

                Spacer(Modifier.weight(1f))

                LinearProgressIndicator(
                    progress = when {
                        totalEpisodeCount > 0 -> watchedEpisodeCount / totalEpisodeCount.toFloat()
                        else -> 0f
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(2.dp))

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

            Divider()
        }
    }
}

private const val TranslucentAppBarAlpha = 0.93f

@Composable
private fun FollowedAppBar(
    loggedIn: Boolean,
    user: TraktUser?,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
    onUserActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = TranslucentAppBarAlpha),
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
                text = stringResource(R.string.following_shows_title),
                style = MaterialTheme.typography.h6,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Spacer(Modifier.weight(1f))

            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                RefreshButton(
                    onClick = onRefreshActionClick,
                    refreshing = refreshing,
                    modifier = Modifier.align(Alignment.CenterVertically),
                )

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
