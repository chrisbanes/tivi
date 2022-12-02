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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import app.tivi.common.compose.ui.PlaceholderPosterCard
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.plus
import app.tivi.data.Entry
import app.tivi.data.resultentities.EntryWithShow
import app.tivi.common.ui.resources.R as UiR

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <E : Entry> EntryGrid(
    lazyPagingItems: LazyPagingItems<out EntryWithShow<E>>,
    title: String,
    onNavigateUp: () -> Unit,
    onOpenShowDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val dismissSnackbarState = rememberDismissState { value ->
        when {
            value != DismissValue.Default -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            }
            else -> false
        }
    }

    lazyPagingItems.loadState.prependErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
        }
    }
    lazyPagingItems.loadState.appendErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
        }
    }
    lazyPagingItems.loadState.refreshErrorOrNull()?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
        }
    }

    Scaffold(
        topBar = {
            EntryGridAppBar(
                title = title,
                onNavigateUp = onNavigateUp,
                refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading,
                onRefreshActionClick = { lazyPagingItems.refresh() },
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = scrollBehavior
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
                        .fillMaxWidth()
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        val refreshing = lazyPagingItems.loadState.refresh == LoadState.Loading
        val refreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = lazyPagingItems::refresh
        )
        Box(modifier = Modifier.pullRefresh(state = refreshState)) {
            val columns = Layout.columns
            val bodyMargin = Layout.bodyMargin
            val gutter = Layout.gutter

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns / 2),
                contentPadding = paddingValues +
                    PaddingValues(horizontal = bodyMargin, vertical = gutter),
                horizontalArrangement = Arrangement.spacedBy(gutter),
                verticalArrangement = Arrangement.spacedBy(gutter),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .bodyWidth()
                    .fillMaxHeight()
            ) {
                items(
                    items = lazyPagingItems,
                    key = { it.show.id }
                ) { entry ->
                    val mod = Modifier
                        .animateItemPlacement()
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
                    fullSpanItem {
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

            PullRefreshIndicator(
                refreshing = refreshing,
                state = refreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(paddingValues),
                scale = true
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryGridAppBar(
    title: String,
    refreshing: Boolean,
    onNavigateUp: () -> Unit,
    onRefreshActionClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(UiR.string.cd_navigate_up)
                )
            }
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = { Text(text = title) },
        actions = {
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
    )
}
