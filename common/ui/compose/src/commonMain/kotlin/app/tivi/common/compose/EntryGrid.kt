// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
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
import androidx.compose.ui.unit.dp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.LazyPagingItems
import app.tivi.common.compose.ui.PlaceholderPosterCard
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.plus
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.Entry
import com.moriatsushi.insetsx.systemBars
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <E : Entry> EntryGrid(
    lazyPagingItems: LazyPagingItems<EntryWithShow<E>>,
    title: String,
    onNavigateUp: () -> Unit,
    onOpenShowDetails: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val dismissSnackbarState = rememberDismissState { value ->
        if (value != DismissValue.Default) {
            snackbarHostState.currentSnackbarData?.dismiss()
            true
        } else {
            false
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
                refreshing = lazyPagingItems.loadState.refresh == LoadStateLoading,
                onRefreshActionClick = { lazyPagingItems.refresh() },
                modifier = Modifier.fillMaxWidth(),
                scrollBehavior = scrollBehavior,
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
        contentWindowInsets = WindowInsets.systemBars,
        modifier = modifier,
    ) { paddingValues ->
        val refreshing = lazyPagingItems.loadState.refresh == LoadStateLoading
        val refreshState = rememberPullRefreshState(
            refreshing = refreshing,
            onRefresh = lazyPagingItems::refresh,
        )
        Box(modifier = Modifier.pullRefresh(state = refreshState)) {
            val columns = Layout.columns
            val bodyMargin = Layout.bodyMargin
            val gutter = Layout.gutter

            LazyVerticalGrid(
                state = rememberLazyGridState(lazyPagingItems.itemCount == 0),
                columns = GridCells.Fixed((columns / 1.5).roundToInt()),
                contentPadding = paddingValues +
                    PaddingValues(horizontal = bodyMargin, vertical = gutter),
                horizontalArrangement = Arrangement.spacedBy(gutter),
                verticalArrangement = Arrangement.spacedBy(gutter),
                flingBehavior = rememberTiviFlingBehavior(),
                modifier = Modifier
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .bodyWidth()
                    .fillMaxHeight(),
            ) {
                items(
                    items = lazyPagingItems,
                    key = { it.show.id },
                ) { entry ->
                    val mod = Modifier
                        .animateItemPlacement()
                        .aspectRatio(2 / 3f)
                        .fillMaxWidth()
                    if (entry != null) {
                        PosterCard(
                            show = entry.show,
                            onClick = { onOpenShowDetails(entry.show.id) },
                            modifier = mod,
                        )
                    } else {
                        PlaceholderPosterCard(mod)
                    }
                }

                if (lazyPagingItems.loadState.append == LoadStateLoading) {
                    fullSpanItem {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
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
                scale = true,
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
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = LocalStrings.current.cdNavigateUp,
                )
            }
        },
        windowInsets = WindowInsets.systemBars
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
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
                modifier = Modifier.align(Alignment.CenterVertically),
            ) { isRefreshing ->
                if (!isRefreshing) {
                    RefreshButton(onClick = onRefreshActionClick)
                }
            }
        },
    )
}
