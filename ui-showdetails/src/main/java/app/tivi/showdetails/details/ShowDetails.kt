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

import android.view.ViewGroup
import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.Providers
import androidx.compose.Recomposer
import androidx.compose.getValue
import androidx.compose.mutableStateOf
import androidx.compose.remember
import androidx.compose.setValue
import androidx.compose.state
import androidx.compose.staticAmbientOf
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.ui.animation.Crossfade
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.ContextAmbient
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.clip
import androidx.ui.core.positionInRoot
import androidx.ui.core.setContent
import androidx.ui.foundation.Box
import androidx.ui.foundation.Icon
import androidx.ui.foundation.ScrollerPosition
import androidx.ui.foundation.Text
import androidx.ui.foundation.VerticalScroller
import androidx.ui.foundation.clickable
import androidx.ui.foundation.contentColor
import androidx.ui.foundation.drawBackground
import androidx.ui.foundation.drawBorder
import androidx.ui.foundation.isSystemInDarkTheme
import androidx.ui.foundation.lazy.LazyRowItems
import androidx.ui.foundation.shape.corner.RoundedCornerShape
import androidx.ui.graphics.Color
import androidx.ui.graphics.ColorFilter
import androidx.ui.layout.Column
import androidx.ui.layout.ConstraintLayout
import androidx.ui.layout.ExperimentalLayout
import androidx.ui.layout.FlowRow
import androidx.ui.layout.Row
import androidx.ui.layout.SizeMode
import androidx.ui.layout.Spacer
import androidx.ui.layout.Stack
import androidx.ui.layout.aspectRatio
import androidx.ui.layout.fillMaxHeight
import androidx.ui.layout.fillMaxSize
import androidx.ui.layout.fillMaxWidth
import androidx.ui.layout.padding
import androidx.ui.layout.preferredHeight
import androidx.ui.layout.preferredHeightIn
import androidx.ui.layout.preferredSize
import androidx.ui.layout.preferredSizeIn
import androidx.ui.layout.preferredWidth
import androidx.ui.layout.preferredWidthIn
import androidx.ui.layout.wrapContentHeight
import androidx.ui.layout.wrapContentSize
import androidx.ui.livedata.observeAsState
import androidx.ui.material.Card
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.ExtendedFloatingActionButton
import androidx.ui.material.IconButton
import androidx.ui.material.LinearProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Snackbar
import androidx.ui.material.Surface
import androidx.ui.material.TopAppBar
import androidx.ui.material.icons.Icons
import androidx.ui.material.icons.filled.ArrowBack
import androidx.ui.material.icons.filled.CloudUpload
import androidx.ui.material.icons.filled.Favorite
import androidx.ui.material.icons.filled.FavoriteBorder
import androidx.ui.material.icons.filled.MoreVert
import androidx.ui.material.icons.filled.Refresh
import androidx.ui.material.icons.filled.Star
import androidx.ui.material.icons.filled.Visibility
import androidx.ui.material.icons.filled.VisibilityOff
import androidx.ui.res.stringResource
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.Dp
import androidx.ui.unit.dp
import app.tivi.common.compose.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ExpandingText
import app.tivi.common.compose.InsetsAmbient
import app.tivi.common.compose.PopupMenu
import app.tivi.common.compose.PopupMenuItem
import app.tivi.common.compose.ProvideInsets
import app.tivi.common.compose.TiviDateFormatterAmbient
import app.tivi.common.compose.VectorImage
import app.tivi.common.compose.onPositionInRootChanged
import app.tivi.common.compose.onSizeChanged
import app.tivi.common.imageloading.TrimTransparentEdgesTransformation
import app.tivi.data.entities.Episode
import app.tivi.data.entities.ImageType
import app.tivi.data.entities.Season
import app.tivi.data.entities.ShowTmdbImage
import app.tivi.data.entities.TiviShow
import app.tivi.data.resultentities.EpisodeWithWatches
import app.tivi.data.resultentities.RelatedShowEntryWithShow
import app.tivi.data.resultentities.SeasonWithEpisodesAndWatches
import app.tivi.data.resultentities.numberAired
import app.tivi.data.resultentities.numberWatched
import app.tivi.data.views.FollowedShowsWatchStats
import app.tivi.ui.animations.lerp
import app.tivi.util.TiviDateFormatter
import coil.request.GetRequest
import dev.chrisbanes.accompanist.coil.CoilImage
import dev.chrisbanes.accompanist.coil.CoilImageWithCrossfade
import dev.chrisbanes.accompanist.mdctheme.MaterialThemeFromMdcTheme

val ShowDetailsTextCreatorAmbient = staticAmbientOf<ShowDetailsTextCreator>()

fun ViewGroup.composeShowDetails(
    state: LiveData<ShowDetailsViewState>,
    pendingUiEffects: LiveData<List<UiEffect>>,
    insets: LiveData<WindowInsetsCompat>,
    actioner: (ShowDetailsAction) -> Unit,
    tiviDateFormatter: TiviDateFormatter,
    textCreator: ShowDetailsTextCreator
): Any = setContent(Recomposer.current()) {
    Providers(
        TiviDateFormatterAmbient provides tiviDateFormatter,
        ShowDetailsTextCreatorAmbient provides textCreator
    ) {
        MaterialThemeFromMdcTheme {
            ProvideInsets(insets) {
                val viewState by state.observeAsState()
                val uiEffects by pendingUiEffects.observeAsState(emptyList())
                if (viewState != null) {
                    ShowDetails(viewState!!, uiEffects, actioner)
                }
            }
        }
    }
}

@Composable
fun ShowDetails(
    viewState: ShowDetailsViewState,
    uiEffects: List<UiEffect>,
    actioner: (ShowDetailsAction) -> Unit
) = ConstraintLayout(
    modifier = Modifier.fillMaxSize()
) {
    val (appbar, fab, snackbar) = createRefs()

    val scrollerPosition = ScrollerPosition()
    var backdropHeight by state { 0 }
    var fabHeight by state { 0 }

    VerticalScroller(
        scrollerPosition = scrollerPosition,
        modifier = Modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            val backdropImage = viewState.backdropImage
            Surface(
                modifier = Modifier.fillMaxWidth()
                    .aspectRatio(16f / 10)
                    .onSizeChanged { backdropHeight = it.height }
            ) {
                if (backdropImage != null) {
                    CoilImageWithCrossfade(
                        data = backdropImage,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),
                elevation = 2.dp
            ) {
                Column(Modifier.fillMaxWidth()) {
                    ShowDetailsAppBar(
                        show = viewState.show,
                        elevation = 0.dp,
                        backgroundColor = Color.Transparent,
                        isRefreshing = viewState.refreshing,
                        actioner = actioner
                    )

                    Row(Modifier.fillMaxWidth()) {
                        val poster = viewState.posterImage
                        if (poster != null) {
                            Spacer(modifier = Modifier.preferredWidth(16.dp))

                            CoilImageWithCrossfade(
                                request = GetRequest.Builder(ContextAmbient.current)
                                    .data(poster)
                                    .build(),
                                alignment = Alignment.TopStart,
                                modifier = Modifier.weight(1f, fill = false)
                                    .aspectRatio(2 / 3f)
                                    .clip(MaterialTheme.shapes.medium)
                            )
                        }

                        Spacer(modifier = Modifier.preferredWidth(16.dp))

                        Box(Modifier.weight(1f, fill = false)) {
                            InfoPanels(viewState.show)
                        }

                        Spacer(modifier = Modifier.preferredWidth(16.dp))
                    }

                    Spacer(modifier = Modifier.preferredHeight(16.dp))

                    Header(stringResource(R.string.details_about))

                    if (viewState.show.summary != null) {
                        ProvideEmphasis(emphasis = EmphasisAmbient.current.high) {
                            ExpandingText(
                                viewState.show.summary!!,
                                modifier = Modifier.fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    val genres = viewState.show.genres
                    if (genres.isNotEmpty()) {
                        Genres(viewState.show)
                    }

                    val nextEpisodeToWatch = viewState.nextEpisodeToWatch()
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

                    val relatedShows = viewState.relatedShows() ?: emptyList()
                    if (relatedShows.isNotEmpty()) {
                        Spacer(modifier = Modifier.preferredHeight(8.dp))
                        Header(stringResource(R.string.details_related))
                        RelatedShows(
                            relatedShows,
                            actioner,
                            Modifier.fillMaxWidth().preferredHeight(112.dp)
                        )
                    }

                    val viewStats = viewState.viewStats()
                    if (viewStats != null) {
                        Spacer(modifier = Modifier.preferredHeight(8.dp))
                        Header(stringResource(R.string.details_view_stats))
                        WatchStats(viewStats)
                    }

                    val seasons = viewState.seasons()
                    if (seasons != null && seasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.preferredHeight(8.dp))
                        Header(stringResource(R.string.show_details_seasons))
                        Seasons(
                            seasons,
                            viewState.expandedSeasonIds,
                            actioner,
                            scrollerPosition,
                            uiEffects.firstOrNull { it is FocusSeasonUiEffect } as? FocusSeasonUiEffect
                        )
                    }

                    // Spacer to push up the content from under the navigation bar
                    val insets = InsetsAmbient.current
                    val spacerHeight = with(DensityAmbient.current) {
                        8.dp + insets.bottom.toDp() + fabHeight.toDp() + 16.dp
                    }
                    Spacer(Modifier.preferredHeight(spacerHeight))
                }
            }
        }
    }

    val insets = InsetsAmbient.current
    val trigger = (backdropHeight - insets.top).coerceAtLeast(0)

    val alpha = lerp(
        startValue = 0.5f,
        endValue = 1f,
        fraction = if (trigger > 0) (scrollerPosition.value / trigger).coerceIn(0f, 1f) else 0f
    )

    Surface(
        color = MaterialTheme.colors.surface.copy(alpha = alpha),
        elevation = if (scrollerPosition.value >= trigger) 2.dp else 0.dp,
        modifier = Modifier.fillMaxWidth().constrainAs(appbar) {
            top.linkTo(parent.top)
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (insets.top > 0) {
                val topInset = with(DensityAmbient.current) { insets.top.toDp() }
                Spacer(Modifier.preferredHeight(topInset).fillMaxWidth())
            }
            if (scrollerPosition.value >= trigger) {
                ShowDetailsAppBar(
                    show = viewState.show,
                    backgroundColor = Color.Transparent,
                    elevation = 0.dp,
                    isRefreshing = viewState.refreshing,
                    actioner = actioner
                )
            }
        }
    }

    Crossfade(current = viewState.refreshError) { error ->
        if (error != null) {
            // TODO: Convert this to swipe-to-dismiss
            Snackbar(
                text = { Text(error.message) },
                modifier = Modifier
                    .preferredWidthIn(maxWidth = 320.dp)
                    .clickable(onClick = { actioner(ClearError) })
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .constrainAs(snackbar) {
                        bottom.linkTo(fab.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )
        }
    }

    val bottomInset = with(DensityAmbient.current) { insets.bottom.toDp() }
    ToggleShowFollowFloatingActionButton(
        isFollowed = viewState.isFollowed,
        onClick = { actioner(FollowShowToggleAction) },
        modifier = Modifier.padding(end = 16.dp, bottom = 16.dp + bottomInset)
            .onSizeChanged { fabHeight = it.height }
            .constrainAs(fab) {
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
    )
}

@Composable
private fun NetworkInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.network_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        val networkLogoPath = show.networkLogoPath
        val networkName = show.network

        if (networkLogoPath != null) {
            val tmdbImage = remember(networkLogoPath) {
                ShowTmdbImage(path = networkLogoPath, type = ImageType.LOGO, showId = 0)
            }

            CoilImage(
                request = GetRequest.Builder(ContextAmbient.current)
                    .data(tmdbImage)
                    .transformations(TrimTransparentEdgesTransformation)
                    .build(),
                contentScale = ContentScale.Fit,
                alignment = Alignment.TopStart,
                colorFilter = if (isSystemInDarkTheme()) ColorFilter.tint(contentColor()) else null,
                modifier = Modifier.preferredSizeIn(maxWidth = 72.dp, maxHeight = 32.dp)
            )
        } else if (networkName != null) {
            Text(
                text = networkName,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Composable
private fun RuntimeInfoPanel(
    show: TiviShow,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.runtime_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        Text(
            text = stringResource(R.string.minutes_format, show.runtime ?: 0),
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
    show: TiviShow,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(
            text = stringResource(R.string.certificate_title),
            style = MaterialTheme.typography.subtitle2
        )

        Spacer(Modifier.preferredHeight(4.dp))

        Text(
            text = show.certification ?: "No certificate",
            style = MaterialTheme.typography.body2,
            modifier = Modifier.drawBorder(
                size = 1.dp,
                color = MaterialTheme.colors.onSurface,
                shape = RoundedCornerShape(2.dp)
            ).padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TraktRatingInfoPanel(
    show: TiviShow,
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
                contentScale = ContentScale.Inside,
                tintColor = MaterialTheme.colors.secondaryVariant,
                modifier = Modifier.preferredSize(32.dp)
            )

            Spacer(Modifier.preferredWidth(4.dp))

            Column {
                Text(
                    text = stringResource(
                        R.string.trakt_rating_text,
                        (show.traktRating ?: 0f) * 10f
                    ),
                    style = MaterialTheme.typography.body2
                )

                Text(
                    text = stringResource(
                        R.string.trakt_rating_votes,
                        (show.traktVotes ?: 0) / 1000f
                    ),
                    style = MaterialTheme.typography.caption
                )
            }
        }
    }
}

@Composable
private fun Header(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.subtitle1,
        modifier = Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun Genres(show: TiviShow) {
    ProvideEmphasis(EmphasisAmbient.current.high) {
        val textCreator = ShowDetailsTextCreatorAmbient.current
        Text(
            textCreator.genreString(show.genres).toString(),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun RelatedShows(
    related: List<RelatedShowEntryWithShow>,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRowItems(
        items = related,
        modifier = modifier
            // TODO: this should be 0.dp and have an initial/last padding
            .padding(horizontal = 14.dp)
    ) { item ->
        Card(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                .fillMaxHeight()
                .aspectRatio(2 / 3f)
        ) {
            Stack(
                Modifier.clickable { actioner(OpenShowDetails(item.show.id)) }
            ) {
                ProvideEmphasis(EmphasisAmbient.current.medium) {
                    Text(
                        text = item.show.title ?: "No title",
                        style = MaterialTheme.typography.caption,
                        modifier = Modifier.padding(4.dp)
                            .gravity(Alignment.CenterStart)
                    )
                }
                val poster = item.poster
                if (poster != null) {
                    CoilImageWithCrossfade(
                        poster,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
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
        modifier = Modifier.fillMaxWidth()
            .preferredHeightIn(minHeight = 48.dp)
            .wrapContentSize(Alignment.CenterStart)
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
        ProvideEmphasis(EmphasisAmbient.current.high) {
            if (show.traktRating != null) {
                TraktRatingInfoPanel(show)
            }
            if (show.network != null || show.networkLogoPath != null) {
                NetworkInfoPanel(show)
            }
            if (show.certification != null) {
                CertificateInfoPanel(show)
            }
            if (show.runtime != null) {
                RuntimeInfoPanel(show)
            }
            if (show.airsDay != null && show.airsTime != null && show.airsTimeZone != null) {
                AirsInfoPanel(show)
            }
        }
    }
}

@Composable
private fun WatchStats(stats: FollowedShowsWatchStats) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 8.dp)
    ) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth(),
            progress = stats.watchedEpisodeCount / stats.episodeCount.toFloat()
        )

        Spacer(modifier = Modifier.preferredHeight(8.dp))

        val textCreator = ShowDetailsTextCreatorAmbient.current

        // TODO: Do something better with CharSequences containing markup/spans
        Text(
            text = textCreator.followedShowEpisodeWatchStatus(stats).toString(),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun Seasons(
    seasons: List<SeasonWithEpisodesAndWatches>,
    expandedSeasonIds: Set<Long>,
    actioner: (ShowDetailsAction) -> Unit,
    scrollerPosition: ScrollerPosition,
    pendingFocusSeasonUiEffect: FocusSeasonUiEffect? = null
) {
    seasons.forEach {
        val mod = if (pendingFocusSeasonUiEffect != null &&
            pendingFocusSeasonUiEffect.seasonId == it.season.id &&
            !scrollerPosition.isAnimating
        ) {
            // Offset, to not scroll the item under the status bar, and leave a gap
            val offset = InsetsAmbient.current.top +
                with(DensityAmbient.current) { 56.dp.toIntPx() }

            Modifier.onPositionInRootChanged { coords ->
                val targetY = coords.positionInRoot.y + scrollerPosition.value - offset

                scrollerPosition.smoothScrollTo(targetY) { _, _ ->
                    actioner(ClearPendingUiEffect(pendingFocusSeasonUiEffect))
                }
            }
        } else Modifier

        SeasonWithEpisodesRow(
            season = it.season,
            episodes = it.episodes,
            expanded = it.season.id in expandedSeasonIds,
            actioner = actioner,
            modifier = mod.fillMaxWidth()
        )
    }
}

@Composable
private fun SeasonWithEpisodesRow(
    season: Season,
    episodes: List<EpisodeWithWatches>,
    expanded: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        elevation = if (expanded) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (expanded) VerticalDivider()

            SeasonRow(
                season,
                episodes,
                actioner,
                modifier = Modifier.fillMaxWidth()
                    .clickable(
                        onClick = { actioner(ChangeSeasonExpandedAction(season.id, !expanded)) },
                        enabled = !season.ignored
                    )
            )

            if (expanded) {
                episodes.forEach { episodeEntry ->
                    EpisodeWithWatchesRow(
                        episodeEntry,
                        modifier = Modifier.fillMaxWidth()
                            .clickable(
                                onClick = {
                                    actioner(OpenEpisodeDetails(episodeEntry.episode!!.id))
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun SeasonRow(
    season: Season,
    episodesWithWatches: List<EpisodeWithWatches>,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.preferredHeightIn(minHeight = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f).gravity(Alignment.CenterVertically)
        ) {
            val textCreator = ShowDetailsTextCreatorAmbient.current

            val emphasis = when {
                season.ignored -> EmphasisAmbient.current.disabled
                else -> EmphasisAmbient.current.high
            }
            ProvideEmphasis(emphasis) {
                Text(
                    text = season.title
                        ?: stringResource(R.string.season_title_fallback, season.number!!),
                    style = MaterialTheme.typography.body1
                )

                Spacer(Modifier.preferredHeight(4.dp))

                Text(
                    text = textCreator.seasonSummaryText(episodesWithWatches).toString(),
                    style = MaterialTheme.typography.caption
                )
            }

            if (!season.ignored) {
                Spacer(Modifier.preferredHeight(4.dp))

                LinearProgressIndicator(
                    episodesWithWatches.numberWatched / episodesWithWatches.size.toFloat(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        val showPopup = state { false }
        SeasonRowOverflowMenu(
            season = season,
            episodesWithWatches = episodesWithWatches,
            popupVisible = showPopup,
            actioner = actioner
        )

        ProvideEmphasis(EmphasisAmbient.current.medium) {
            IconButton(onClick = { showPopup.value = true }) {
                Icon(Icons.Default.MoreVert)
            }
        }
    }
}

@Composable
private fun EpisodeWithWatchesRow(
    episodeWithWatches: EpisodeWithWatches,
    modifier: Modifier = Modifier
) {
    val episode = episodeWithWatches.episode!!

    Row(
        modifier = modifier.preferredHeightIn(minHeight = 48.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val textCreator = ShowDetailsTextCreatorAmbient.current

            ProvideEmphasis(EmphasisAmbient.current.high) {
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

        ProvideEmphasis(EmphasisAmbient.current.medium) {
            var needSpacer = false
            if (episodeWithWatches.hasPending()) {
                Icon(
                    asset = Icons.Default.CloudUpload,
                    modifier = Modifier.gravity(Alignment.CenterVertically)
                )
                needSpacer = true
            }
            if (episodeWithWatches.isWatched()) {
                if (needSpacer) {
                    Spacer(Modifier.preferredWidth(4.dp))
                }
                Icon(
                    asset = when {
                        episodeWithWatches.onlyPendingDeletes() -> Icons.Default.VisibilityOff
                        else -> Icons.Default.Visibility
                    },
                    modifier = Modifier.gravity(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun VerticalDivider(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.preferredHeight(Dp.Hairline)
            .fillMaxWidth()
            .drawBackground(contentColor().copy(alpha = 0.15f))
    )
}

@Composable
private fun SeasonRowOverflowMenu(
    season: Season,
    episodesWithWatches: List<EpisodeWithWatches>,
    popupVisible: MutableState<Boolean>,
    actioner: (ShowDetailsAction) -> Unit
) {
    val items = ArrayList<PopupMenuItem>()

    items += if (season.ignored) {
        PopupMenuItem(
            title = stringResource(id = R.string.popup_season_follow),
            onClick = { actioner(ChangeSeasonFollowedAction(season.id, true)) }
        )
    } else {
        PopupMenuItem(
            title = stringResource(id = R.string.popup_season_ignore),
            onClick = { actioner(ChangeSeasonFollowedAction(season.id, false)) }
        )
    }

    // Season number starts from 1, rather than 0
    if (season.number ?: -100 >= 2) {
        items += PopupMenuItem(
            title = stringResource(id = R.string.popup_season_ignore_previous),
            onClick = { actioner(UnfollowPreviousSeasonsFollowedAction(season.id)) }
        )
    }

    if (episodesWithWatches.numberWatched > 0) {
        items += PopupMenuItem(
            title = stringResource(id = R.string.popup_season_mark_all_unwatched),
            onClick = { actioner(MarkSeasonUnwatchedAction(season.id)) }
        )
    }

    if (episodesWithWatches.numberWatched < episodesWithWatches.size) {
        items += PopupMenuItem(
            title = stringResource(id = R.string.popup_season_mark_watched_all),
            onClick = { actioner(MarkSeasonWatchedAction(season.id)) }
        )
    }

    if (episodesWithWatches.numberWatched < episodesWithWatches.numberAired &&
        episodesWithWatches.numberAired < episodesWithWatches.size
    ) {
        items += PopupMenuItem(
            title = stringResource(id = R.string.popup_season_mark_watched_aired),
            onClick = { actioner(MarkSeasonWatchedAction(season.id, onlyAired = true)) }
        )
    }

    PopupMenu(
        items = items,
        visible = popupVisible,
        alignment = Alignment.CenterEnd
    )
}

@Composable
private fun ShowDetailsAppBar(
    show: TiviShow,
    elevation: Dp,
    backgroundColor: Color,
    isRefreshing: Boolean,
    actioner: (ShowDetailsAction) -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(text = show.title ?: "")
        },
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
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
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
        modifier = modifier
    )
}

private val previewShow = TiviShow(title = "Detective Penny")

@Preview
@Composable
private fun PreviewSeasonRow() {
    SeasonRowOverflowMenu(
        season = Season(showId = 0, number = 1, ignored = false),
        episodesWithWatches = emptyList(),
        popupVisible = mutableStateOf(false),
        actioner = {}
    )
}

@Preview
@Composable
private fun PreviewTopAppBar() {
    ShowDetailsAppBar(
        show = previewShow,
        elevation = 1.dp,
        backgroundColor = MaterialTheme.colors.surface,
        isRefreshing = true,
        actioner = {}
    )
}
