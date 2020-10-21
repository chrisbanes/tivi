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
import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Icon
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ConstraintLayout
import androidx.compose.foundation.layout.ExperimentalLayout
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.SizeMode
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredHeightIn
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.layout.preferredSizeIn
import androidx.compose.foundation.layout.preferredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.onCommit
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticAmbientOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.onSizeChanged
import androidx.compose.ui.platform.ContextAmbient
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import app.tivi.common.compose.AbsoluteElevationSurface
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.Carousel
import app.tivi.common.compose.ExpandableFloatingActionButton
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.IconResource
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.LogCompositions
import app.tivi.common.compose.PosterCard
import app.tivi.common.compose.SwipeDismissSnackbar
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.navigationBarsPadding
import app.tivi.common.compose.offset
import app.tivi.common.compose.rememberMutableState
import app.tivi.common.compose.statusBarsHeight
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
import app.tivi.ui.animations.lerp
import coil.request.ImageRequest
import dev.chrisbanes.accompanist.coil.CoilImage
import kotlinx.coroutines.launch
import org.threeten.bp.OffsetDateTime

val ShowDetailsTextCreatorAmbient = staticAmbientOf<ShowDetailsTextCreator>()

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ShowDetails(
    viewState: ShowDetailsViewState,
    actioner: (ShowDetailsAction) -> Unit
) = ConstraintLayout(
    modifier = Modifier.fillMaxSize()
) {
    LogCompositions("ShowDetails")

    val (appbar, fab, snackbar) = createRefs()

    val scrollState = rememberScrollState()
    var backdropHeight by rememberMutableState { 0 }

    ScrollableColumn(
        scrollState = scrollState,
        modifier = Modifier.fillMaxHeight()
    ) {
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
            scrollState = scrollState,
            actioner = actioner,
            onBackdropSizeChanged = { backdropHeight = it.height }
        )
    }

    OverlaidStatusBarAppBar(
        scrollPosition = scrollState.value,
        backdropHeight = backdropHeight,
        appBar = {
            ShowDetailsAppBar(
                title = viewState.show.title ?: "",
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                isRefreshing = viewState.refreshing,
                actioner = actioner
            )
        },
        modifier = Modifier.fillMaxWidth()
            .constrainAs(appbar) {
                top.linkTo(parent.top)
            }
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

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
            .constrainAs(snackbar) {
                bottom.linkTo(fab.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            }
    )

    onCommit(viewState.refreshError) {
        viewState.refreshError?.let { error ->
            snackbarScope.launch {
                snackbarHostState.showSnackbar(error.message)
            }
        }
    }

    ToggleShowFollowFloatingActionButton(
        isFollowed = viewState.isFollowed,
        expanded = scrollState.value < backdropHeight,
        onClick = { actioner(FollowShowToggleAction) },
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding(bottom = false)
            .constrainAs(fab) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
    )
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
    scrollState: ScrollState,
    actioner: (ShowDetailsAction) -> Unit,
    onBackdropSizeChanged: (IntSize) -> Unit
) {
    LogCompositions("ShowDetailsScrollingContent")

    Column(Modifier.fillMaxWidth()) {
        AbsoluteElevationSurface(
            modifier = Modifier.fillMaxWidth()
                .aspectRatio(16f / 10)
                .onSizeChanged(onBackdropSizeChanged)
        ) {
            if (backdropImage != null) {
                CoilImage(
                    data = backdropImage,
                    fadeIn = true,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().offset { size ->
                        Offset(
                            x = 0f,
                            y = (scrollState.value / 2)
                                .coerceIn(-size.height.toFloat(), size.height.toFloat())
                        )
                    }
                )
            }
        }

        AbsoluteElevationSurface(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(Alignment.Top),
            elevation = 2.dp
        ) {
            Column(Modifier.fillMaxWidth()) {
                ShowDetailsAppBar(
                    title = show.title ?: "",
                    elevation = 0.dp,
                    backgroundColor = Color.Transparent,
                    isRefreshing = showRefreshing,
                    actioner = actioner
                )

                Row(Modifier.fillMaxWidth()) {
                    if (posterImage != null) {
                        Spacer(modifier = Modifier.preferredWidth(16.dp))

                        CoilImage(
                            data = posterImage,
                            fadeIn = true,
                            alignment = Alignment.TopStart,
                            modifier = Modifier.weight(1f, fill = false)
                                .aspectRatio(2 / 3f)
                                .clip(MaterialTheme.shapes.medium)
                        )
                    }

                    Spacer(modifier = Modifier.preferredWidth(16.dp))

                    Box(Modifier.weight(1f, fill = false)) {
                        InfoPanels(show)
                    }

                    Spacer(modifier = Modifier.preferredWidth(16.dp))
                }

                Spacer(modifier = Modifier.preferredHeight(16.dp))

                Header(stringResource(R.string.details_about))

                if (show.summary != null) {
                    ProvideEmphasis(emphasis = AmbientEmphasisLevels.current.high) {
                        ExpandingText(
                            show.summary!!,
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                if (show.genres.isNotEmpty()) {
                    Genres(show.genres)
                }

                if (nextEpisodeToWatch?.episode != null && nextEpisodeToWatch.season != null) {
                    Spacer(modifier = Modifier.preferredHeight(8.dp))
                    Header(stringResource(id = R.string.details_next_episode_to_watch))
                    NextEpisodeToWatch(
                        season = nextEpisodeToWatch.season!!,
                        episode = nextEpisodeToWatch.episode!!,
                        onClick = {
                            actioner(OpenEpisodeDetails(nextEpisodeToWatch.episode!!.id))
                        }
                    )
                }

                if (relatedShows.isNotEmpty()) {
                    Spacer(modifier = Modifier.preferredHeight(8.dp))
                    Header(stringResource(R.string.details_related))
                    RelatedShows(
                        relatedShows,
                        actioner,
                        Modifier.fillMaxWidth().preferredHeight(112.dp)
                    )
                }

                if (watchStats != null) {
                    Spacer(modifier = Modifier.preferredHeight(8.dp))
                    Header(stringResource(R.string.details_view_stats))
                    WatchStats(watchStats.watchedEpisodeCount, watchStats.episodeCount)
                }

                if (seasons.isNotEmpty()) {
                    Spacer(modifier = Modifier.preferredHeight(8.dp))
                    Header(stringResource(R.string.show_details_seasons))
                    Seasons(seasons, expandedSeasonIds, actioner)
                }

                // Spacer to push up content from under the FloatingActionButton
                Spacer(Modifier.preferredHeight(56.dp + 16.dp + 16.dp))
            }
        }
    }
}

@Composable
private fun OverlaidStatusBarAppBar(
    scrollPosition: Float,
    backdropHeight: Int,
    modifier: Modifier = Modifier,
    appBar: @Composable () -> Unit
) {
    LogCompositions("OverlaidStatusBarAppBar")

    val insets = InsetsAmbient.current
    val trigger = (backdropHeight - insets.systemBars.top).coerceAtLeast(0)

    val alpha = lerp(
        startValue = 0.5f,
        endValue = 1f,
        fraction = if (trigger > 0) (scrollPosition / trigger).coerceIn(0f, 1f) else 0f
    )

    AbsoluteElevationSurface(
        color = MaterialTheme.colors.surface.copy(alpha = alpha),
        elevation = if (scrollPosition >= trigger) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth()) {
            Spacer(Modifier.statusBarsHeight())
            if (scrollPosition >= trigger) {
                appBar()
            }
        }
    }
}

@Composable
private fun NetworkInfoPanel(
    networkName: String,
    networkLogoPath: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.network_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        if (networkLogoPath != null) {
            val tmdbImage = remember(networkLogoPath) {
                ShowTmdbImage(path = networkLogoPath, type = ImageType.LOGO, showId = 0)
            }

            CoilImage(
                request = ImageRequest.Builder(ContextAmbient.current)
                    .data(tmdbImage)
                    .transformations(TrimTransparentEdgesTransformation)
                    .build(),
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopStart,
                colorFilter = when {
                    isSystemInDarkTheme() -> ColorFilter.tint(AmbientContentColor.current)
                    else -> null
                },
                modifier = Modifier.preferredSizeIn(maxWidth = 72.dp, maxHeight = 32.dp)
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

        Spacer(Modifier.preferredHeight(4.dp))

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

        Spacer(Modifier.preferredHeight(4.dp))

        val textCreator = ShowDetailsTextCreatorAmbient.current
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

        Spacer(Modifier.preferredHeight(4.dp))

        val textCreator = ShowDetailsTextCreatorAmbient.current
        Text(
            text = textCreator.airsText(show)?.toString() ?: "No air date",
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

        Spacer(Modifier.preferredHeight(4.dp))

        Text(
            text = certification,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface,
                shape = RoundedCornerShape(2.dp)
            ).padding(horizontal = 4.dp, vertical = 2.dp)
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

        Spacer(Modifier.preferredHeight(4.dp))

        Row {
            VectorImage(
                vector = Icons.Default.Star,
                contentScale = ContentScale.Fit,
                tintColor = MaterialTheme.colors.secondaryVariant,
                modifier = Modifier.preferredSize(32.dp)
            )

            Spacer(Modifier.preferredWidth(4.dp))

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
        modifier = Modifier.fillMaxWidth()
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
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        ProvideEmphasis(AmbientEmphasisLevels.current.high) {
            val textCreator = ShowDetailsTextCreatorAmbient.current
            Text(
                textCreator.genreString(genres).toString(),
                style = MaterialTheme.typography.body2
            )
        }
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
        modifier = Modifier.fillMaxWidth()
            .preferredHeightIn(min = 48.dp)
            .wrapContentHeight()
            .clickable(onClick = onClick)
            .padding(16.dp, 8.dp)
    ) {
        val textCreator = ShowDetailsTextCreatorAmbient.current

        Text(
            textCreator.seasonEpisodeTitleText(season, episode),
            style = MaterialTheme.typography.caption
        )

        Spacer(modifier = Modifier.preferredHeight(4.dp))

        Text(
            episode.title ?: stringResource(R.string.episode_title_fallback, episode.number!!),
            style = MaterialTheme.typography.body1
        )
    }
}

@OptIn(ExperimentalLayout::class)
@Composable
private fun InfoPanels(show: TiviShow) {
    FlowRow(
        mainAxisSize = SizeMode.Expand,
        mainAxisSpacing = 8.dp,
        crossAxisSpacing = 8.dp
    ) {
        ProvideEmphasis(AmbientEmphasisLevels.current.high) {
            if (show.traktRating != null) {
                TraktRatingInfoPanel(show.traktRating!!, show.traktVotes ?: 0)
            }
            if (show.network != null) {
                NetworkInfoPanel(show.network!!, show.networkLogoPath)
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
}

@Composable
private fun WatchStats(
    watchedEpisodeCount: Int,
    episodeCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
    ) {
        LinearProgressIndicator(
            progress = when {
                episodeCount > 0 -> watchedEpisodeCount / episodeCount.toFloat()
                else -> 0f
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.preferredHeight(8.dp))

        val textCreator = ShowDetailsTextCreatorAmbient.current

        // TODO: Do something better with CharSequences containing markup/spans
        Text(
            text = "${textCreator.followedShowEpisodeWatchStatus(watchedEpisodeCount, episodeCount)}",
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun Seasons(
    seasons: List<SeasonWithEpisodesAndWatches>,
    expandedSeasonIds: Set<Long>,
    actioner: (ShowDetailsAction) -> Unit
) {
    LogCompositions("Seasons")

    seasons.forEach {
        SeasonWithEpisodesRow(
            season = it.season,
            episodes = it.episodes,
            expanded = it.season.id in expandedSeasonIds,
            actioner = actioner,
            modifier = Modifier.fillMaxWidth()
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
    AbsoluteElevationSurface(
        elevation = if (expanded) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (expanded) Divider()

            SeasonRow(
                season,
                episodes.numberAired,
                episodes.numberWatched,
                episodes.numberAiredToWatch,
                episodes.numberToAir,
                episodes.nextToAir?.firstAired,
                actioner,
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        onClick = { actioner(ChangeSeasonExpandedAction(season.id, !expanded)) },
                        enabled = !season.ignored
                    )
            )

            episodes.forEach { episodeEntry ->
                AnimatedVisibility(visible = expanded) {
                    EpisodeWithWatchesRow(
                        episodeEntry.episode,
                        episodeEntry.isWatched,
                        episodeEntry.hasPending,
                        episodeEntry.onlyPendingDeletes,
                        modifier = Modifier.fillMaxWidth()
                            .clickable {
                                actioner(OpenEpisodeDetails(episodeEntry.episode.id))
                            }
                    )
                }
            }
        }
    }
}

@Suppress("UNUSED_PARAMETER")
@Composable
private fun SeasonRow(
    season: Season,
    episodesAired: Int,
    episodesWatched: Int,
    episodesToWatch: Int,
    episodesToAir: Int,
    nextToAirDate: OffsetDateTime? = null,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.preferredHeightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).align(Alignment.CenterVertically)
        ) {
            val textCreator = ShowDetailsTextCreatorAmbient.current

            val emphasis = when {
                season.ignored -> AmbientEmphasisLevels.current.disabled
                else -> AmbientEmphasisLevels.current.high
            }
            ProvideEmphasis(emphasis) {
                Text(
                    text = season.title
                        ?: stringResource(R.string.season_title_fallback, season.number!!),
                    style = MaterialTheme.typography.body1
                )

                Spacer(Modifier.preferredHeight(4.dp))

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
                Spacer(Modifier.preferredHeight(4.dp))

                LinearProgressIndicator(
                    episodesWatched / episodesAired.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        var showMenu by rememberMutableState { false }

        DropdownMenu(
            toggle = {
                ProvideEmphasis(AmbientEmphasisLevels.current.medium) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert)
                    }
                }
            },
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
        modifier = modifier.preferredHeightIn(min = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = ShowDetailsTextCreatorAmbient.current

            ProvideEmphasis(AmbientEmphasisLevels.current.high) {
                Text(
                    text = textCreator.episodeNumberText(episode).toString(),
                    style = MaterialTheme.typography.caption
                )

                Spacer(Modifier.preferredHeight(2.dp))

                Text(
                    text = episode.title
                        ?: stringResource(R.string.episode_title_fallback, episode.number!!),
                    style = MaterialTheme.typography.body2
                )
            }
        }

        ProvideEmphasis(AmbientEmphasisLevels.current.medium) {
            var needSpacer = false
            if (hasPending) {
                IconResource(
                    resourceId = R.drawable.ic_cloud_upload,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
                needSpacer = true
            }
            if (isWatched) {
                if (needSpacer) {
                    Spacer(Modifier.preferredWidth(4.dp))
                }
                IconResource(
                    resourceId = when {
                        onlyPendingDeletes -> R.drawable.ic_visibility_off
                        else -> R.drawable.ic_visibility
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
                Icon(Icons.Default.ArrowBack)
            }
        },
        actions = {
            if (isRefreshing) {
                AutoSizedCircularProgressIndicator(
                    modifier = Modifier.aspectRatio(1f)
                        .fillMaxHeight()
                        .padding(14.dp)
                )
            } else {
                IconButton(onClick = { actioner(RefreshAction) }) {
                    Icon(Icons.Default.Refresh)
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
    expanded: Boolean = true,
    modifier: Modifier = Modifier
) {
    LogCompositions("ToggleShowFollowFloatingActionButton")

    ExpandableFloatingActionButton(
        onClick = onClick,
        icon = {
            Icon(
                when {
                    isFollowed -> Icons.Default.FavoriteBorder
                    else -> Icons.Default.Favorite
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
        expanded = expanded,
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
