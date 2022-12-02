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

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.showdetails.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.gutterSpacer
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.Backdrop
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.imageloading.TrimTransparentEdgesTransformation
import app.tivi.data.entities.Episode
import app.tivi.data.entities.Genre
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.Season
import app.tivi.data.entities.ShowStatus
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.data.resultentities.EpisodeWithSeason
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.resultentities.nextToAir
import app.tivi.data.resultentities.numberAired
import app.tivi.data.resultentities.numberAiredToWatch
import app.tivi.data.resultentities.numberToAir
import app.tivi.data.resultentities.numberWatched
import app.tivi.data.views.FollowedShowsWatchStats
import com.google.accompanist.flowlayout.FlowRow
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.SnapOffsets
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior
import org.threeten.bp.OffsetDateTime
import app.tivi.common.ui.resources.R as UiR

@Composable
fun ShowDetails(
    navigateUp: () -> Unit,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    openSeasons: (showId: Long, seasonId: Long) -> Unit
) {
    ShowDetails(
        viewModel = hiltViewModel(),
        navigateUp = navigateUp,
        openShowDetails = openShowDetails,
        openEpisodeDetails = openEpisodeDetails,
        openSeasons = openSeasons
    )
}

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
internal fun ShowDetails(
    viewModel: ShowDetailsViewModel,
    navigateUp: () -> Unit,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    openSeasons: (showId: Long, seasonId: Long) -> Unit
) {
    val viewState by viewModel.state.collectAsState()
    ShowDetails(
        viewState = viewState,
        navigateUp = navigateUp,
        openShowDetails = openShowDetails,
        openEpisodeDetails = openEpisodeDetails,
        refresh = viewModel::refresh,
        onMessageShown = viewModel::clearMessage,
        openSeason = { openSeasons(viewState.show.id, it) },
        onSeasonFollowed = { viewModel.setSeasonFollowed(it, true) },
        onSeasonUnfollowed = { viewModel.setSeasonFollowed(it, false) },
        unfollowPreviousSeasons = viewModel::unfollowPreviousSeasons,
        onMarkSeasonWatched = { viewModel.setSeasonWatched(it, onlyAired = true) },
        onMarkSeasonUnwatched = viewModel::setSeasonUnwatched,
        onToggleShowFollowed = viewModel::toggleFollowShow
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun ShowDetails(
    viewState: ShowDetailsViewState,
    navigateUp: () -> Unit,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    refresh: () -> Unit,
    onMessageShown: (id: Long) -> Unit,
    openSeason: (seasonId: Long) -> Unit,
    onSeasonFollowed: (seasonId: Long) -> Unit,
    onSeasonUnfollowed: (seasonId: Long) -> Unit,
    unfollowPreviousSeasons: (seasonId: Long) -> Unit,
    onMarkSeasonWatched: (seasonId: Long) -> Unit,
    onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
    onToggleShowFollowed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()

    val dismissSnackbarState = rememberDismissState { value ->
        when {
            value != DismissValue.Default -> {
                snackbarHostState.currentSnackbarData?.dismiss()
                true
            }
            else -> false
        }
    }

    viewState.message?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message.message)
            // Notify the view model that the message has been dismissed
            onMessageShown(message.id)
        }
    }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        topBar = {
            ShowDetailsAppBar(
                title = viewState.show.title ?: "",
                isRefreshing = viewState.refreshing,
                onNavigateUp = navigateUp,
                onRefresh = refresh,
                scrollBehavior = scrollBehavior,
                modifier = Modifier.fillMaxWidth()
            )
        },
        floatingActionButton = {
            val expanded by remember {
                derivedStateOf { listState.firstVisibleItemIndex > 0 }
            }

            ToggleShowFollowFloatingActionButton(
                isFollowed = viewState.isFollowed,
                expanded = expanded,
                onClick = onToggleShowFollowed
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
        // The nav bar is handled by the root Scaffold
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .exclude(WindowInsets.navigationBars),
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LogCompositions("ShowDetails")

        Surface(modifier = Modifier.bodyWidth()) {
            ShowDetailsScrollingContent(
                show = viewState.show,
                posterImage = viewState.posterImage,
                backdropImage = viewState.backdropImage,
                relatedShows = viewState.relatedShows,
                nextEpisodeToWatch = viewState.nextEpisodeToWatch,
                seasons = viewState.seasons,
                watchStats = viewState.watchStats,
                listState = listState,
                openShowDetails = openShowDetails,
                openEpisodeDetails = openEpisodeDetails,
                contentPadding = contentPadding,
                openSeason = openSeason,
                onSeasonFollowed = onSeasonFollowed,
                onSeasonUnfollowed = onSeasonUnfollowed,
                unfollowPreviousSeasons = unfollowPreviousSeasons,
                onMarkSeasonWatched = onMarkSeasonWatched,
                onMarkSeasonUnwatched = onMarkSeasonUnwatched,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ShowDetailsScrollingContent(
    show: TiviShow,
    posterImage: TmdbImageEntity?,
    backdropImage: TmdbImageEntity?,
    relatedShows: List<RelatedShowEntryWithShow>,
    nextEpisodeToWatch: EpisodeWithSeason?,
    seasons: List<SeasonWithEpisodesAndWatches>,
    watchStats: FollowedShowsWatchStats?,
    listState: LazyListState,
    openShowDetails: (showId: Long) -> Unit,
    openEpisodeDetails: (episodeId: Long) -> Unit,
    openSeason: (seasonId: Long) -> Unit,
    onSeasonFollowed: (seasonId: Long) -> Unit,
    onSeasonUnfollowed: (seasonId: Long) -> Unit,
    unfollowPreviousSeasons: (seasonId: Long) -> Unit,
    onMarkSeasonWatched: (seasonId: Long) -> Unit,
    onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    LogCompositions("ShowDetailsScrollingContent")

    val gutter = Layout.gutter
    val bodyMargin = Layout.bodyMargin

    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = modifier
    ) {
        item {
            Backdrop(
                imageModel = backdropImage,
                modifier = Modifier
                    .padding(horizontal = bodyMargin, vertical = gutter)
                    .fillMaxWidth()
                    .aspectRatio(16f / 10)
            )
        }

        item {
            Spacer(modifier = Modifier.height(max(gutter, bodyMargin)))
        }

        item {
            PosterInfoRow(
                show = show,
                posterImage = posterImage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        gutterSpacer()

        item {
            Header(stringResource(UiR.string.details_about))
        }

        if (show.summary != null) {
            item {
                ExpandingText(
                    text = show.summary!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
                )
            }
        }

        if (show.genres.isNotEmpty()) {
            item {
                Genres(show.genres)
            }
        }

        if (nextEpisodeToWatch?.episode != null && nextEpisodeToWatch.season != null) {
            gutterSpacer()

            item {
                Header(stringResource(id = UiR.string.details_next_episode_to_watch))
            }
            item {
                NextEpisodeToWatch(
                    season = nextEpisodeToWatch.season!!,
                    episode = nextEpisodeToWatch.episode!!,
                    onClick = { openEpisodeDetails(nextEpisodeToWatch.episode!!.id) }
                )
            }
        }

        if (relatedShows.isNotEmpty()) {
            gutterSpacer()

            item {
                Header(stringResource(UiR.string.details_related))
            }
            item {
                RelatedShows(
                    related = relatedShows,
                    openShowDetails = openShowDetails,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (watchStats != null) {
            gutterSpacer()

            item {
                Header(stringResource(UiR.string.details_view_stats))
            }
            item {
                WatchStats(watchStats.watchedEpisodeCount, watchStats.episodeCount)
            }
        }

        if (seasons.isNotEmpty()) {
            gutterSpacer()

            item {
                Header(stringResource(UiR.string.show_details_seasons))
            }

            items(items = seasons) { season ->
                SeasonRow(
                    season = season.season,
                    episodesAired = season.episodes.numberAired,
                    episodesWatched = season.episodes.numberWatched,
                    episodesToWatch = season.episodes.numberAiredToWatch,
                    episodesToAir = season.episodes.numberToAir,
                    nextToAirDate = season.episodes.nextToAir?.firstAired,
                    contentPadding = PaddingValues(horizontal = bodyMargin, vertical = gutter),
                    openSeason = openSeason,
                    onSeasonFollowed = onSeasonFollowed,
                    onSeasonUnfollowed = onSeasonUnfollowed,
                    unfollowPreviousSeasons = unfollowPreviousSeasons,
                    onMarkSeasonWatched = onMarkSeasonWatched,
                    onMarkSeasonUnwatched = onMarkSeasonUnwatched,
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }

        // Spacer to push up content from under the FloatingActionButton
        itemSpacer(56.dp + 32.dp)
    }
}

@Composable
private fun PosterInfoRow(
    show: TiviShow,
    posterImage: TmdbImageEntity?,
    modifier: Modifier = Modifier
) {
    Row(modifier.padding(horizontal = Layout.bodyMargin)) {
        AsyncImage(
            model = posterImage,
            requestBuilder = { crossfade(true) },
            contentDescription = stringResource(UiR.string.cd_show_poster, show.title ?: ""),
            modifier = Modifier
                .weight(1f)
                .aspectRatio(2 / 3f)
                .clip(MaterialTheme.shapes.medium),
            alignment = Alignment.TopStart
        )

        InfoPanels(
            show = show,
            modifier = Modifier
                .weight(1f)
                .padding(start = Layout.gutter)
        )
    }
}

@Composable
private fun NetworkInfoPanel(
    networkName: String,
    modifier: Modifier = Modifier,
    networkLogoPath: String? = null
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.network_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        if (networkLogoPath != null) {
            val tmdbImage = remember(networkLogoPath) {
                ShowTmdbImage(path = networkLogoPath, type = ImageType.LOGO, showId = 0)
            }

            AsyncImage(
                model = tmdbImage,
                requestBuilder = {
                    crossfade(true)
                    transformations(TrimTransparentEdgesTransformation)
                },
                contentDescription = stringResource(UiR.string.cd_network_logo),
                modifier = Modifier.sizeIn(maxWidth = 72.dp, maxHeight = 32.dp),
                alignment = Alignment.TopStart,
                contentScale = ContentScale.Fit,
                colorFilter = when {
                    isSystemInDarkTheme() -> ColorFilter.tint(LocalContentColor.current)
                    else -> null
                }
            )
        } else {
            Text(
                text = networkName,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun RuntimeInfoPanel(
    runtime: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.runtime_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(UiR.string.minutes_format, runtime),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ShowStatusPanel(
    showStatus: ShowStatus,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.status_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalTiviTextCreator.current
        Text(
            text = textCreator.showStatusText(showStatus).toString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AirsInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.airs_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalTiviTextCreator.current
        Text(
            text = textCreator.airsText(show).toString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun CertificateInfoPanel(
    certification: String,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.certificate_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = certification,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TraktRatingInfoPanel(
    rating: Float,
    votes: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(UiR.string.trakt_rating_title),
            style = MaterialTheme.typography.titleSmall
        )

        Spacer(Modifier.height(4.dp))

        Row {
            Image(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
                modifier = Modifier.size(32.dp)
            )

            Spacer(Modifier.width(4.dp))

            Column {
                Text(
                    text = stringResource(
                        UiR.string.trakt_rating_text,
                        rating * 10f
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = stringResource(
                        UiR.string.trakt_rating_votes,
                        votes / 1000f
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun Header(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun Genres(genres: List<Genre>) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        val textCreator = LocalTiviTextCreator.current
        Text(
            text = textCreator.genreString(genres).toString(),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@OptIn(ExperimentalSnapperApi::class, ExperimentalFoundationApi::class)
@Composable
private fun RelatedShows(
    related: List<RelatedShowEntryWithShow>,
    openShowDetails: (showId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LogCompositions("RelatedShows")

    val lazyListState = rememberLazyListState()
    val contentPadding = PaddingValues(horizontal = Layout.bodyMargin, vertical = Layout.gutter)

    LazyRow(
        state = lazyListState,
        modifier = modifier,
        flingBehavior = rememberSnapperFlingBehavior(
            lazyListState = lazyListState,
            snapOffsetForItem = SnapOffsets.Start
        ),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = related,
            key = { it.show.id }
        ) { item ->
            PosterCard(
                show = item.show,
                poster = item.poster,
                onClick = { openShowDetails(item.show.id) },
                modifier = Modifier
                    .animateItemPlacement()
                    .fillParentMaxWidth(0.21f) // 21% of the available width
                    .aspectRatio(2 / 3f)
            )
        }
    }
}

@Composable
private fun NextEpisodeToWatch(
    season: Season,
    episode: Episode,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        val textCreator = LocalTiviTextCreator.current

        Text(
            textCreator.seasonEpisodeTitleText(season, episode),
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.height(4.dp))

        Text(
            episode.title ?: stringResource(UiR.string.episode_title_fallback, episode.number!!),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun InfoPanels(
    show: TiviShow,
    modifier: Modifier = Modifier
) {
    FlowRow(
        mainAxisSpacing = Layout.gutter,
        crossAxisSpacing = Layout.gutter,
        modifier = modifier
    ) {
        if (show.traktRating != null) {
            TraktRatingInfoPanel(show.traktRating!!, show.traktVotes ?: 0)
        }
        if (show.network != null) {
            NetworkInfoPanel(networkName = show.network!!, networkLogoPath = show.networkLogoPath)
        }
        if (show.status != null) {
            ShowStatusPanel(show.status!!)
        }
        if (show.certification != null) {
            CertificateInfoPanel(show.certification!!)
        }
        if (show.runtime != null) {
            RuntimeInfoPanel(show.runtime!!)
        }
        if (show.airsDay != null && show.airsTime != null && show.airsTimeZone != null &&
            show.status == ShowStatus.IN_PRODUCTION
        ) {
            AirsInfoPanel(show)
        }
    }
}

@Composable
private fun WatchStats(
    watchedEpisodeCount: Int,
    episodeCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
    ) {
        LinearProgressIndicator(
            progress = when {
                episodeCount > 0 -> watchedEpisodeCount / episodeCount.toFloat()
                else -> 0f
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Layout.gutter))

        val textCreator = LocalTiviTextCreator.current

        // TODO: Do something better with CharSequences containing markup/spans
        Text(
            text = "${textCreator.followedShowEpisodeWatchStatus(watchedEpisodeCount, episodeCount)}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    episodesAired: Int,
    episodesWatched: Int,
    episodesToWatch: Int,
    episodesToAir: Int,
    openSeason: (seasonId: Long) -> Unit,
    onSeasonFollowed: (seasonId: Long) -> Unit,
    onSeasonUnfollowed: (seasonId: Long) -> Unit,
    unfollowPreviousSeasons: (seasonId: Long) -> Unit,
    onMarkSeasonWatched: (seasonId: Long) -> Unit,
    onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    nextToAirDate: OffsetDateTime? = null
) {
    Row(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(enabled = !season.ignored) {
                openSeason(season.id)
            }
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(contentPadding)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            val textCreator = LocalTiviTextCreator.current

            Text(
                text = season.title
                    ?: stringResource(UiR.string.season_title_fallback, season.number!!),
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = textCreator.seasonSummaryText(
                    watched = episodesWatched,
                    toWatch = episodesToWatch,
                    toAir = episodesToAir,
                    nextToAirDate = nextToAirDate
                ).toString(),
                style = MaterialTheme.typography.bodySmall
            )

            if (!season.ignored && episodesAired > 0) {
                LinearProgressIndicator(
                    progress = episodesWatched / episodesAired.toFloat(),
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                )
            }
        }

        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            var showMenu by remember { mutableStateOf(false) }

            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(UiR.string.cd_open_overflow)
                )
            }

            SeasonDropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                season = season,
                episodesAired = episodesAired,
                episodesWatched = episodesWatched,
                episodesToAir = episodesToAir,
                onSeasonFollowed = onSeasonFollowed,
                onSeasonUnfollowed = onSeasonUnfollowed,
                unfollowPreviousSeasons = unfollowPreviousSeasons,
                onMarkSeasonWatched = onMarkSeasonWatched,
                onMarkSeasonUnwatched = onMarkSeasonUnwatched
            )
        }
    }
}

@Composable
private fun SeasonDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    season: Season,
    episodesAired: Int,
    episodesWatched: Int,
    episodesToAir: Int,
    onSeasonFollowed: (seasonId: Long) -> Unit,
    onSeasonUnfollowed: (seasonId: Long) -> Unit,
    unfollowPreviousSeasons: (seasonId: Long) -> Unit,
    onMarkSeasonWatched: (seasonId: Long) -> Unit,
    onMarkSeasonUnwatched: (seasonId: Long) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        if (season.ignored) {
            DropdownMenuItem(
                text = { Text(text = stringResource(UiR.string.popup_season_follow)) },
                onClick = {
                    onSeasonFollowed(season.id)
                    onDismissRequest()
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(text = stringResource(UiR.string.popup_season_ignore)) },
                onClick = {
                    onSeasonUnfollowed(season.id)
                    onDismissRequest()
                }
            )
        }

        // Season number starts from 1, rather than 0
        if ((season.number ?: -100) >= 2) {
            DropdownMenuItem(
                text = { Text(text = stringResource(UiR.string.popup_season_ignore_previous)) },
                onClick = {
                    unfollowPreviousSeasons(season.id)
                    onDismissRequest()
                }
            )
        }

        if (episodesWatched > 0) {
            DropdownMenuItem(
                text = { Text(text = stringResource(UiR.string.popup_season_mark_all_unwatched)) },
                onClick = {
                    onMarkSeasonUnwatched(season.id)
                    onDismissRequest()
                }
            )
        }

        if (episodesWatched < episodesAired) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = if (episodesToAir == 0) {
                            stringResource(id = UiR.string.popup_season_mark_watched_all)
                        } else {
                            stringResource(id = UiR.string.popup_season_mark_watched_aired)
                        }
                    )
                },
                onClick = {
                    onMarkSeasonWatched(season.id)
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun ShowDetailsAppBar(
    title: String,
    isRefreshing: Boolean,
    onNavigateUp: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LogCompositions("ShowDetailsAppBar")

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onNavigateUp) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(UiR.string.cd_navigate_up)
                )
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(16.dp)
                )
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(UiR.string.cd_refresh)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(),
        scrollBehavior = scrollBehavior,
        modifier = modifier
    )
}

@Composable
private fun ToggleShowFollowFloatingActionButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    expanded: Boolean,
    modifier: Modifier = Modifier
) {
    LogCompositions("ToggleShowFollowFloatingActionButton")

    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = when {
                    isFollowed -> Icons.Default.Favorite
                    else -> Icons.Default.FavoriteBorder
                },
                contentDescription = when {
                    isFollowed -> stringResource(UiR.string.cd_follow_show_remove)
                    else -> stringResource(UiR.string.cd_follow_show_add)
                }
            )
        },
        text = {
            Text(
                when {
                    isFollowed -> stringResource(UiR.string.follow_show_remove)
                    else -> stringResource(UiR.string.follow_show_add)
                }
            )
        },
        containerColor = when {
            isFollowed -> FloatingActionButtonDefaults.containerColor
            else -> MaterialTheme.colorScheme.surface
        },
        expanded = expanded,
        modifier = modifier
    )
}
