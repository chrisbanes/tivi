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

package app.tivi.showdetails.seasons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.lifecycle.SavedStateHandle
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.TopAppBarWithBottomContent
import app.tivi.common.compose.viewModel
import app.tivi.common.ui.resources.R as UiR
import app.tivi.data.compoundmodels.EpisodeWithWatches
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

typealias ShowSeasons = @Composable (
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    initialSeasonId: Long?,
) -> Unit

@Inject
@Composable
fun ShowSeasons(
    viewModelFactory: (SavedStateHandle) -> ShowSeasonsViewModel,
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    initialSeasonId: Long? = null,
) {
    ShowSeasons(
        viewModel = viewModel(factory = viewModelFactory),
        navigateUp = navigateUp,
        openEpisodeDetails = openEpisodeDetails,
        initialSeasonId = initialSeasonId,
    )
}

@Composable
internal fun ShowSeasons(
    viewModel: ShowSeasonsViewModel,
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    initialSeasonId: Long?,
) {
    val viewState by viewModel.state.collectAsState()

    ShowSeasons(
        viewState = viewState,
        navigateUp = navigateUp,
        openEpisodeDetails = openEpisodeDetails,
        refresh = viewModel::refresh,
        onMessageShown = viewModel::clearMessage,
        initialSeasonId = initialSeasonId,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun ShowSeasons(
    viewState: ShowSeasonsViewState,
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    refresh: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
    initialSeasonId: Long?,
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

    viewState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    val pagerState = rememberPagerState()

    var pagerBeenScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) pagerBeenScrolled = true
    }

    if (initialSeasonId != null && !pagerBeenScrolled && pagerState.canScrollForward) {
        val initialIndex = viewState.seasons.indexOfFirst { it.season.id == initialSeasonId }
        LaunchedEffect(initialIndex) {
            pagerState.scrollToPage(initialIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.statusBars,
        topBar = {
            TopAppBarWithBottomContent(
                title = { Text(text = viewState.show.title ?: "") },
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(UiR.string.cd_navigate_up),
                        )
                    }
                },
                actions = {
                    RefreshButton(
                        refreshing = viewState.refreshing,
                        onClick = refresh,
                    )
                },
                bottomContent = {
                    SeasonPagerTabs(
                        pagerState = pagerState,
                        seasons = viewState.seasons.map { it.season },
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = Color.Transparent,
                        contentColor = LocalContentColor.current,
                    )
                },
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
        modifier = Modifier
            .testTag("show_seasons")
            .fillMaxSize(),
    ) { contentPadding ->
        SeasonsPager(
            seasons = viewState.seasons,
            pagerState = pagerState,
            openEpisodeDetails = openEpisodeDetails,
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxHeight()
                .bodyWidth(),
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeasonPagerTabs(
    pagerState: PagerState,
    seasons: List<Season>,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = contentColorFor(containerColor),
) {
    if (seasons.isEmpty()) return

    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        containerColor = containerColor,
        contentColor = contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
            )
        },
        modifier = modifier,
    ) {
        // Add tabs for all of our pages
        seasons.forEachIndexed { index, season ->
            Tab(
                text = { Text(text = LocalTiviTextCreator.current.seasonTitle(season)) },
                selected = pagerState.currentPage == index,
                onClick = {
                    // Animate to the selected page when clicked
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SeasonsPager(
    seasons: List<SeasonWithEpisodesAndWatches>,
    pagerState: PagerState,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        pageCount = seasons.size,
        state = pagerState,
        modifier = modifier,
    ) { page ->
        EpisodesList(
            episodes = seasons[page].episodes,
            onEpisodeClick = openEpisodeDetails,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun EpisodesList(
    episodes: List<EpisodeWithWatches>,
    onEpisodeClick: (episodeId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        items(episodes, key = { it.episode.id }) { item ->
            EpisodeWithWatchesRow(
                episode = item.episode,
                isWatched = item.hasWatches,
                hasPending = item.hasPending,
                onlyPendingDeletes = item.onlyPendingDeletes,
                modifier = Modifier
                    .testTag("show_seasons_episode_item")
                    .fillParentMaxWidth()
                    .clickable { onEpisodeClick(item.episode.id) },
            )
        }
    }
}

@Composable
private fun EpisodeWithWatchesRow(
    episode: Episode,
    isWatched: Boolean,
    hasPending: Boolean,
    onlyPendingDeletes: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.episodeNumberText(episode).toString(),
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = episode.title
                    ?: stringResource(UiR.string.episode_title_fallback, episode.number!!),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        var needSpacer = false
        if (hasPending) {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = stringResource(UiR.string.cd_episode_syncing),
                modifier = Modifier.align(Alignment.CenterVertically),
            )
            needSpacer = true
        }
        if (isWatched) {
            if (needSpacer) Spacer(Modifier.width(4.dp))

            Icon(
                imageVector = when {
                    onlyPendingDeletes -> Icons.Default.VisibilityOff
                    else -> Icons.Default.Visibility
                },
                contentDescription = when {
                    onlyPendingDeletes -> stringResource(UiR.string.cd_episode_deleted)
                    else -> stringResource(UiR.string.cd_episode_watched)
                },
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
    pageIndexMapping: (Int) -> Int = { it },
): Modifier = layout { measurable, constraints ->
    if (tabPositions.isEmpty()) {
        // If there are no pages, nothing to show
        layout(constraints.maxWidth, 0) {}
    } else {
        val currentPage = minOf(tabPositions.lastIndex, pageIndexMapping(pagerState.currentPage))
        val currentTab = tabPositions[currentPage]
        val previousTab = tabPositions.getOrNull(currentPage - 1)
        val nextTab = tabPositions.getOrNull(currentPage + 1)
        val fraction = pagerState.currentPageOffsetFraction
        val indicatorWidth = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.width, nextTab.width, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.width, previousTab.width, -fraction).roundToPx()
        } else {
            currentTab.width.roundToPx()
        }
        val indicatorOffset = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.left, nextTab.left, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.left, previousTab.left, -fraction).roundToPx()
        } else {
            currentTab.left.roundToPx()
        }
        val placeable = measurable.measure(
            Constraints(
                minWidth = indicatorWidth,
                maxWidth = indicatorWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight,
            ),
        )
        layout(constraints.maxWidth, maxOf(placeable.height, constraints.minHeight)) {
            placeable.placeRelative(
                indicatorOffset,
                maxOf(constraints.minHeight - placeable.height, 0),
            )
        }
    }
}
