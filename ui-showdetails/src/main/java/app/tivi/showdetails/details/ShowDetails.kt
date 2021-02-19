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

package app.tivi.showdetails.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.Carousel
import app.tivi.common.compose.ExpandableFloatingActionButton
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.SimpleFlowRow
import app.tivi.common.compose.SwipeDismissSnackbar
import app.tivi.common.compose.foregroundColor
import app.tivi.common.compose.itemSpacer
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
import app.tivi.data.resultentities.EpisodeWithWatches
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.resultentities.nextToAir
import app.tivi.data.resultentities.numberAired
import app.tivi.data.resultentities.numberAiredToWatch
import app.tivi.data.resultentities.numberToAir
import app.tivi.data.resultentities.numberWatched
import app.tivi.data.views.FollowedShowsWatchStats
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.insets.LocalWindowInsets
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsHeight
import org.threeten.bp.OffsetDateTime

val LocalShowDetailsTextCreator = staticCompositionLocalOf<ShowDetailsTextCreator> {
    error("ShowDetailsTextCreator not provided")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowDetails(
    viewState: ShowDetailsViewState,
    actioner: (ShowDetailsAction) -> Unit
) = Box(modifier = Modifier.fillMaxSize()) {
    LogCompositions("ShowDetails")

    val listState = rememberLazyListState()
    var backdropHeight by remember { mutableStateOf(0) }

    Surface(Modifier.fillMaxSize()) {
        ShowDetailsScrollingContent(
            show = viewState.show,
            posterImage = viewState.posterImage,
            backdropImage = viewState.backdropImage,
            relatedShows = viewState.relatedShows,
            nextEpisodeToWatch = viewState.nextEpisodeToWatch,
            seasons = viewState.seasons,
            expandedSeasonIds = viewState.expandedSeasonIds,
            watchStats = viewState.watchStats,
            showRefreshing = viewState.refreshing,
            listState = listState,
            actioner = actioner,
            onBackdropSizeChanged = { backdropHeight = it.height },
            modifier = Modifier.fillMaxSize()
        )
    }

    val trigger = backdropHeight - LocalWindowInsets.current.statusBars.top

    OverlaidStatusBarAppBar(
        showAppBar = {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset >= trigger
        },
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
    ) {
        ShowDetailsAppBar(
            title = viewState.show.title ?: "",
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            isRefreshing = viewState.refreshing,
            actioner = actioner
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    Column(
        Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            snackbar = {
                SwipeDismissSnackbar(
                    data = it,
                    onDismiss = { actioner(ClearError) }
                )
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )

        val expanded by remember {
            derivedStateOf { listState.firstVisibleItemIndex > 0 }
        }

        ToggleShowFollowFloatingActionButton(
            isFollowed = viewState.isFollowed,
            expanded = { expanded },
            onClick = { actioner(FollowShowToggleAction) },
            modifier = Modifier
                .align(Alignment.End)
                .padding(16.dp)
                .navigationBarsPadding(bottom = false)
        )
    }

    LaunchedEffect(viewState.refreshError) {
        viewState.refreshError?.let { error ->
            snackbarHostState.showSnackbar(error.message)
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
    expandedSeasonIds: Set<Long>,
    watchStats: FollowedShowsWatchStats?,
    showRefreshing: Boolean,
    listState: LazyListState,
    actioner: (ShowDetailsAction) -> Unit,
    onBackdropSizeChanged: (IntSize) -> Unit,
    modifier: Modifier = Modifier,
) {
    LogCompositions("ShowDetailsScrollingContent")

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        item {
            BackdropImage(
                backdropImage = backdropImage,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 10)
                    .onSizeChanged(onBackdropSizeChanged)
                    .clipToBounds()
                    .offset {
                        IntOffset(
                            x = 0,
                            y = if (listState.firstVisibleItemIndex == 0) {
                                listState.firstVisibleItemScrollOffset / 2
                            } else 0
                        )
                    }
            )
        }

        item {
            ShowDetailsAppBar(
                title = show.title ?: "",
                elevation = 0.dp,
                backgroundColor = Color.Transparent,
                isRefreshing = showRefreshing,
                actioner = actioner
            )
        }

        item {
            PosterInfoRow(
                show = show,
                posterImage = posterImage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        itemSpacer(16.dp)

        item {
            Header(stringResource(R.string.details_about))
        }

        if (show.summary != null) {
            item {
                ExpandingText(
                    text = show.summary!!,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (show.genres.isNotEmpty()) {
            item {
                Genres(show.genres)
            }
        }

        if (nextEpisodeToWatch?.episode != null && nextEpisodeToWatch.season != null) {
            itemSpacer(8.dp)

            item {
                Header(stringResource(id = R.string.details_next_episode_to_watch))
            }
            item {
                NextEpisodeToWatch(
                    season = nextEpisodeToWatch.season!!,
                    episode = nextEpisodeToWatch.episode!!,
                    onClick = { actioner(OpenEpisodeDetails(nextEpisodeToWatch.episode!!.id)) }
                )
            }
        }

        if (relatedShows.isNotEmpty()) {
            itemSpacer(8.dp)

            item {
                Header(stringResource(R.string.details_related))
            }
            item {
                RelatedShows(
                    related = relatedShows,
                    actioner = actioner,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                )
            }
        }

        if (watchStats != null) {
            itemSpacer(8.dp)

            item {
                Header(stringResource(R.string.details_view_stats))
            }
            item {
                WatchStats(watchStats.watchedEpisodeCount, watchStats.episodeCount)
            }
        }

        if (seasons.isNotEmpty()) {
            itemSpacer(8.dp)

            item {
                Header(stringResource(R.string.show_details_seasons))
            }

            items(seasons) { season ->
                SeasonWithEpisodesRow(
                    season = season.season,
                    episodes = season.episodes,
                    expanded = season.season.id in expandedSeasonIds,
                    actioner = actioner,
                    modifier = Modifier.fillParentMaxWidth(),
                )
            }
        }

        // Spacer to push up content from under the FloatingActionButton
        itemSpacer(56.dp + 16.dp + 16.dp)
    }
}

@Composable
private fun PosterInfoRow(
    show: TiviShow,
    posterImage: TmdbImageEntity?,
    modifier: Modifier = Modifier
) {
    Row(modifier) {
        if (posterImage != null) {
            Spacer(Modifier.width(16.dp))

            CoilImage(
                data = posterImage,
                fadeIn = true,
                contentDescription = stringResource(R.string.cd_show_poster, show.title ?: ""),
                alignment = Alignment.TopStart,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .aspectRatio(2 / 3f)
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(Modifier.weight(1f, fill = false)) {
            InfoPanels(show)
        }

        Spacer(modifier = Modifier.width(16.dp))
    }
}

@Composable
private fun BackdropImage(
    backdropImage: TmdbImageEntity?,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        if (backdropImage != null) {
            CoilImage(
                data = backdropImage,
                contentDescription = stringResource(R.string.cd_show_poster),
                fadeIn = true,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        // TODO show a placeholder if null
    }
}

@Composable
private fun OverlaidStatusBarAppBar(
    showAppBar: () -> Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    LogCompositions("OverlaidStatusBarAppBar")

    Column(modifier) {
        val transition = updateOverlaidStatusBarAppBarTransition(showAppBar())

        Surface(
            elevation = transition.elevation,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsHeight()
                .graphicsLayer {
                    alpha = transition.alpha
                    translationY = transition.offset
                },
            content = {}
        )

        if (showAppBar()) {
            Surface(
                elevation = transition.elevation,
                modifier = Modifier.fillMaxWidth(),
                content = content,
            )
        }
    }
}

@Composable
private fun updateOverlaidStatusBarAppBarTransition(
    showAppBar: Boolean
): OverlaidStatusBarAppBarTransition {
    LogCompositions("updateOverlaidStatusBarAppBarTransition")

    val transition = updateTransition(showAppBar)

    val elevation = transition.animateDp { show -> if (show) 2.dp else 0.dp }

    val alpha = transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> snap()
                else -> tween(durationMillis = 300)
            }
        }
    ) { show ->
        if (show) 1f else 0f
    }

    val offset = transition.animateFloat(
        transitionSpec = {
            when {
                false isTransitioningTo true -> spring()
                // This is a bit of a hack. We don't actually want an offset transition
                // on exit, so we just run a snap AFTER the alpha animation
                // has finished (with some buffer)
                else -> snap(delayMillis = 320)
            }
        }
    ) { show ->
        if (show) 0f else LocalWindowInsets.current.statusBars.top.toFloat()
    }

    return remember(transition) {
        OverlaidStatusBarAppBarTransition(elevation, alpha, offset)
    }
}

@Stable
class OverlaidStatusBarAppBarTransition(
    elevation: State<Dp>,
    alpha: State<Float>,
    offset: State<Float>,
) {
    val elevation: Dp by elevation
    val alpha: Float by alpha
    val offset: Float by offset
}

@Composable
private fun NetworkInfoPanel(
    networkName: String,
    modifier: Modifier = Modifier,
    networkLogoPath: String? = null,
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.network_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        if (networkLogoPath != null) {
            val tmdbImage = remember(networkLogoPath) {
                ShowTmdbImage(path = networkLogoPath, type = ImageType.LOGO, showId = 0)
            }

            CoilImage(
                data = tmdbImage,
                requestBuilder = {
                    transformations(TrimTransparentEdgesTransformation)
                },
                contentDescription = stringResource(R.string.cd_network_logo),
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopStart,
                colorFilter = when {
                    isSystemInDarkTheme() -> ColorFilter.tint(foregroundColor())
                    else -> null
                },
                modifier = Modifier.sizeIn(maxWidth = 72.dp, maxHeight = 32.dp)
            )
        } else {
            Text(
                text = networkName,
                style = MaterialTheme.typography.body2
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
            text = stringResource(R.string.runtime_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.minutes_format, runtime),
            style = MaterialTheme.typography.body2
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
            text = stringResource(R.string.status_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalShowDetailsTextCreator.current
        Text(
            text = textCreator.showStatusText(showStatus).toString(),
            style = MaterialTheme.typography.body2
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
            text = stringResource(R.string.airs_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        val textCreator = LocalShowDetailsTextCreator.current
        Text(
            text = textCreator.airsText(show).toString(),
            style = MaterialTheme.typography.body2
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
            text = stringResource(R.string.certificate_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = certification,
            style = MaterialTheme.typography.body2,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onSurface,
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
            text = stringResource(R.string.trakt_rating_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.height(4.dp))

        Row {
            Image(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(MaterialTheme.colors.secondaryVariant),
                modifier = Modifier.size(32.dp),
            )

            Spacer(Modifier.width(4.dp))

            Column {
                Text(
                    text = stringResource(
                        R.string.trakt_rating_text,
                        rating * 10f
                    ),
                    style = MaterialTheme.typography.body2
                )

                Text(
                    text = stringResource(
                        R.string.trakt_rating_votes,
                        votes / 1000f
                    ),
                    style = MaterialTheme.typography.caption
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.subtitle1
        )
    }
}

@Composable
private fun Genres(genres: List<Genre>) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val textCreator = LocalShowDetailsTextCreator.current
        Text(
            textCreator.genreString(genres).toString(),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun RelatedShows(
    related: List<RelatedShowEntryWithShow>,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LogCompositions("RelatedShows")

    Carousel(
        items = related,
        contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
        itemSpacing = 4.dp,
        modifier = modifier
    ) { item, padding ->
        PosterCard(
            show = item.show,
            poster = item.poster,
            onClick = { actioner(OpenShowDetails(item.show.id)) },
            modifier = Modifier
                .padding(padding)
                .fillParentMaxHeight()
                .aspectRatio(2 / 3f)
        )
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
            .padding(16.dp, 8.dp)
    ) {
        val textCreator = LocalShowDetailsTextCreator.current

        Text(
            textCreator.seasonEpisodeTitleText(season, episode),
            style = MaterialTheme.typography.caption
        )

        Spacer(Modifier.height(4.dp))

        Text(
            episode.title ?: stringResource(R.string.episode_title_fallback, episode.number!!),
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
private fun InfoPanels(show: TiviShow) {
    SimpleFlowRow(
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp,
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
        if (show.airsDay != null && show.airsTime != null && show.airsTimeZone != null) {
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
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
    ) {
        LinearProgressIndicator(
            progress = when {
                episodeCount > 0 -> watchedEpisodeCount / episodeCount.toFloat()
                else -> 0f
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        val textCreator = LocalShowDetailsTextCreator.current

        // TODO: Do something better with CharSequences containing markup/spans
        Text(
            text = "${textCreator.followedShowEpisodeWatchStatus(watchedEpisodeCount, episodeCount)}",
            style = MaterialTheme.typography.body2
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SeasonWithEpisodesRow(
    season: Season,
    episodes: List<EpisodeWithWatches>,
    expanded: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val elevation by animateDpAsState(if (expanded) 2.dp else 0.dp)
    Surface(
        elevation = elevation,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth()) {
            SeasonRow(
                season = season,
                episodesAired = episodes.numberAired,
                episodesWatched = episodes.numberWatched,
                episodesToWatch = episodes.numberAiredToWatch,
                episodesToAir = episodes.numberToAir,
                nextToAirDate = episodes.nextToAir?.firstAired,
                actioner = actioner,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !season.ignored) {
                        actioner(ChangeSeasonExpandedAction(season.id, !expanded))
                    }
            )

            // Ideally each EpisodeWithWatchesRow would be in a different item {}, but there
            // are currently 2 issues for that:
            // #1: AnimatedVisibility currently crashes in Lazy*: b/170287733
            // #2: Can't use a Surface across different items: b/170472398
            // So instead we bundle the items in an inner Column, within a single item.
            episodes.forEach { episodeEntry ->
                AnimatedVisibility(visible = expanded) {
                    EpisodeWithWatchesRow(
                        episode = episodeEntry.episode,
                        isWatched = episodeEntry.isWatched,
                        hasPending = episodeEntry.hasPending,
                        onlyPendingDeletes = episodeEntry.onlyPendingDeletes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                actioner(OpenEpisodeDetails(episodeEntry.episode.id))
                            }
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Divider()
            }
        }
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    episodesAired: Int,
    episodesWatched: Int,
    episodesToWatch: Int,
    episodesToAir: Int,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier,
    nextToAirDate: OffsetDateTime? = null,
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            val textCreator = LocalShowDetailsTextCreator.current

            val contentAlpha = when {
                season.ignored -> ContentAlpha.disabled
                else -> ContentAlpha.high
            }
            CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                Text(
                    text = season.title
                        ?: stringResource(R.string.season_title_fallback, season.number!!),
                    style = MaterialTheme.typography.body1
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = textCreator.seasonSummaryText(
                        episodesWatched,
                        episodesToWatch,
                        episodesToAir,
                        nextToAirDate
                    ).toString(),
                    style = MaterialTheme.typography.caption
                )
            }

            if (!season.ignored && episodesAired > 0) {
                Spacer(Modifier.height(4.dp))

                LinearProgressIndicator(
                    episodesWatched / episodesAired.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        var showMenu by remember { mutableStateOf(false) }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    stringResource(R.string.cd_open_overflow)
                )
            }
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (season.ignored) {
                DropdownMenuItem(
                    onClick = { actioner(ChangeSeasonFollowedAction(season.id, true)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_follow))
                }
            } else {
                DropdownMenuItem(
                    onClick = { actioner(ChangeSeasonFollowedAction(season.id, false)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_ignore))
                }
            }

            // Season number starts from 1, rather than 0
            if (season.number ?: -100 >= 2) {
                DropdownMenuItem(
                    onClick = { actioner(UnfollowPreviousSeasonsFollowedAction(season.id)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_ignore_previous))
                }
            }

            if (episodesWatched > 0) {
                DropdownMenuItem(
                    onClick = { actioner(MarkSeasonUnwatchedAction(season.id)) }
                ) {
                    Text(text = stringResource(id = R.string.popup_season_mark_all_unwatched))
                }
            }

            if (episodesWatched < episodesAired) {
                if (episodesToAir == 0) {
                    DropdownMenuItem(
                        onClick = { actioner(MarkSeasonWatchedAction(season.id)) }
                    ) {
                        Text(text = stringResource(id = R.string.popup_season_mark_watched_all))
                    }
                } else {
                    DropdownMenuItem(
                        onClick = { actioner(MarkSeasonWatchedAction(season.id, onlyAired = true)) }
                    ) {
                        Text(text = stringResource(id = R.string.popup_season_mark_watched_aired))
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeWithWatchesRow(
    episode: Episode,
    isWatched: Boolean,
    hasPending: Boolean,
    onlyPendingDeletes: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = LocalShowDetailsTextCreator.current

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
                    painter = painterResource(R.drawable.ic_cloud_upload),
                    contentDescription = stringResource(R.string.cd_episode_syncing),
                    modifier = Modifier.align(Alignment.CenterVertically),
                )
                needSpacer = true
            }
            if (isWatched) {
                if (needSpacer) Spacer(Modifier.width(4.dp))

                Icon(
                    painter = painterResource(
                        when {
                            onlyPendingDeletes -> R.drawable.ic_visibility_off
                            else -> R.drawable.ic_visibility
                        }
                    ),
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

@Composable
private fun ShowDetailsAppBar(
    title: String,
    elevation: Dp,
    backgroundColor: Color,
    isRefreshing: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LogCompositions("ShowDetailsAppBar")

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { actioner(NavigateUp) }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.cd_navigate_up)
                )
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                )
            } else {
                IconButton(onClick = { actioner(RefreshAction) }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh)
                    )
                }
            }
        },
        elevation = elevation,
        backgroundColor = backgroundColor,
        modifier = modifier
    )
}

@Composable
private fun ToggleShowFollowFloatingActionButton(
    isFollowed: Boolean,
    onClick: () -> Unit,
    expanded: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    LogCompositions("ToggleShowFollowFloatingActionButton")

    ExpandableFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                imageVector = when {
                    isFollowed -> Icons.Default.FavoriteBorder
                    else -> Icons.Default.Favorite
                },
                contentDescription = when {
                    isFollowed -> stringResource(R.string.cd_follow_show_remove)
                    else -> stringResource(R.string.cd_follow_show_add)
                }
            )
        },
        text = {
            Text(
                when {
                    isFollowed -> stringResource(R.string.follow_show_remove)
                    else -> stringResource(R.string.follow_show_add)
                }
            )
        },
        backgroundColor = when {
            isFollowed -> MaterialTheme.colors.surface
            else -> MaterialTheme.colors.primary
        },
        expanded = expanded(),
        modifier = modifier
    )
}

private val previewShow = TiviShow(title = "Detective Penny")

@Preview
@Composable
private fun PreviewTopAppBar() {
    ShowDetailsAppBar(
        title = previewShow.title ?: "",
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface,
        isRefreshing = true,
        actioner = {}
    )
}
