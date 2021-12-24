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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Tab
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.primarySurface
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.rememberFlowWithLifecycle
import app.tivi.common.compose.theme.AppBarAlphas
import app.tivi.common.compose.ui.SwipeDismissSnackbar
import app.tivi.common.compose.ui.TopAppBarWithBottomContent
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Season
import app.tivi.data.resultentities.EpisodeWithWatches
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.LocalScaffoldPadding
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
fun ShowSeasons(
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    initialSeasonId: Long? = null,
) {
    ShowSeasons(
        viewModel = hiltViewModel(),
        navigateUp = navigateUp,
        openEpisodeDetails = openEpisodeDetails,
        initialSeasonId = initialSeasonId,
    )
}

@OptIn(ExperimentalPagerApi::class)
@Composable
internal fun ShowSeasons(
    viewModel: ShowSeasonsViewModel,
    navigateUp: () -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    initialSeasonId: Long?,
) {
    val viewState by rememberFlowWithLifecycle(viewModel.state)
        .collectAsState(initial = ShowSeasonsViewState.Empty)

    val scaffoldState = rememberScaffoldState()

    LaunchedEffect(viewState.refreshError) {
        viewState.refreshError?.let { error ->
            scaffoldState.snackbarHostState.showSnackbar(error.message)
        }
    }

    val pagerState = rememberPagerState()

    var pagerBeenScrolled by remember { mutableStateOf(false) }
    LaunchedEffect(pagerState.isScrollInProgress) {
        if (pagerState.isScrollInProgress) pagerBeenScrolled = true
    }

    LaunchedEffect(initialSeasonId, viewState.seasons, pagerBeenScrolled) {
        if (initialSeasonId != null && !pagerBeenScrolled) {
            val initialIndex = viewState.seasons.indexOfFirst { it.season.id == initialSeasonId }
            if (initialIndex >= 0) {
                pagerState.scrollToPage(initialIndex)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBarWithBottomContent(
                title = { Text(text = viewState.show.title ?: "") },
                contentPadding = rememberInsetsPaddingValues(
                    insets = LocalWindowInsets.current.systemBars,
                    applyBottom = false
                ),
                navigationIcon = {
                    IconButton(onClick = navigateUp) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.cd_navigate_up)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface.copy(
                    alpha = AppBarAlphas.translucentBarAlpha()
                ),
                bottomContent = {
                    SeasonPagerTabs(
                        pagerState = pagerState,
                        seasons = viewState.seasons.map { it.season },
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.Transparent,
                        contentColor = LocalContentColor.current,
                    )
                }
            )
        },
        snackbarHost = { snackbarHostState ->
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    SwipeDismissSnackbar(
                        data = snackbarData,
                        onDismiss = { viewModel.clearError() }
                    )
                },
                modifier = Modifier
                    .padding(horizontal = Layout.bodyMargin)
                    .fillMaxWidth()
            )
        }
    ) {
        SeasonsPager(
            seasons = viewState.seasons,
            pagerState = pagerState,
            openEpisodeDetails = openEpisodeDetails,
            modifier = Modifier
                .fillMaxHeight()
                .bodyWidth(),
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SeasonPagerTabs(
    pagerState: PagerState,
    seasons: List<Season>,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.primarySurface,
    contentColor: Color = contentColorFor(backgroundColor),
) {
    if (pagerState.pageCount == 0) return

    val coroutineScope = rememberCoroutineScope()

    ScrollableTabRow(
        // Our selected tab is our current page
        selectedTabIndex = pagerState.currentPage,
        backgroundColor = backgroundColor,
        contentColor = contentColor,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
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
                }
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun SeasonsPager(
    seasons: List<SeasonWithEpisodesAndWatches>,
    pagerState: PagerState,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        count = seasons.size,
        state = pagerState,
        modifier = modifier,
    ) { page ->
        val season = seasons[page]
        EpisodesList(
            episodes = season.episodes,
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
    LazyColumn(
        modifier = modifier,
        contentPadding = LocalScaffoldPadding.current,
    ) {
        items(episodes, key = { it.episode.id }) { item ->
            EpisodeWithWatchesRow(
                episode = item.episode,
                isWatched = item.hasWatches,
                hasPending = item.hasPending,
                onlyPendingDeletes = item.onlyPendingDeletes,
                modifier = Modifier
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
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = textCreator.episodeNumberText(episode).toString(),
                style = MaterialTheme.typography.caption
            )

            Spacer(Modifier.height(2.dp))

            Text(
                text = episode.title
                    ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                style = MaterialTheme.typography.body2
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            var needSpacer = false
            if (hasPending) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = stringResource(R.string.cd_episode_syncing),
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
                        onlyPendingDeletes -> stringResource(R.string.cd_episode_deleted)
                        else -> stringResource(R.string.cd_episode_watched)
                    },
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
