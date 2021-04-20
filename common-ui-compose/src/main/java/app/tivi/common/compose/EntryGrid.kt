/*
 * Copyright 2021 Google LLC
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

package app.tivi.common.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun <E : Entry> EntryGrid(
    lazyPagingItems: LazyPagingItems<out EntryWithShow<E>>,
    title: String,
    onOpenShowDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            EntryGridAppBar(
                title = title,
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
                onRefreshActionClick = { lazyPagingItems.refresh() },
                modifier = Modifier.fillMaxWidth()
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(
                isRefreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
            ),
            onRefresh = { lazyPagingItems.refresh() },
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
                modifier = Modifier.fillMaxSize(),
            ) {
                itemsInGrid(
                    lazyPagingItems = lazyPagingItems,
                    columns = 3,
                    contentPadding = PaddingValues(4.dp),
                    verticalItemPadding = 2.dp,
                    horizontalItemPadding = 2.dp
                ) { entry ->
                    val mod = Modifier
                        .aspectRatio(2 / 3f)
                        .fillMaxWidth()
                    if (entry != null) {
                        PosterCard(
                            show = entry.show,
                            poster = entry.poster,
                            onClick = { onOpenShowDetails(entry.show.id) },
                            modifier = mod
                        )
                    } else {
                        PlaceholderPosterCard(mod)
                    }
                }

                if (lazyPagingItems.loadState.append == LoadState.Loading) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            CircularProgressIndicator(Modifier.align(Alignment.Center))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EntryGridAppBar(
    title: String,
    refreshing: Boolean,
    onRefreshActionClick: () -> Unit,
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
                text = title,
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
            }
        }
    }
}
