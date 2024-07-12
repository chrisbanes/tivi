// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.showdetails.details

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPositionInLayout
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.StartToStart
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.gutterSpacer
import app.tivi.common.compose.itemSpacer
import app.tivi.common.compose.rememberSnapFlingBehavior
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.Backdrop
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.ScrimmedIconButton
import app.tivi.common.compose.ui.copy
import app.tivi.common.compose.ui.noIndicationClickable
import app.tivi.common.compose.ui.rememberShowImageModel
import app.tivi.data.compoundmodels.EpisodeWithSeason
import app.tivi.data.compoundmodels.RelatedShowEntryWithShow
import app.tivi.data.compoundmodels.SeasonWithEpisodesAndWatches
import app.tivi.data.models.Episode
import app.tivi.data.models.Genre
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.ShowStatus
import app.tivi.data.models.TiviShow
import app.tivi.data.views.ShowsWatchStats
import app.tivi.screens.ShowDetailsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject

@Inject
class ShowDetailsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is ShowDetailsScreen -> {
      ui<ShowDetailsUiState> { state, modifier ->
        ShowDetails(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun ShowDetails(
  state: ShowDetailsUiState,
  modifier: Modifier = Modifier,
) {
  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  ShowDetails(
    viewState = state,
    navigateUp = { eventSink(ShowDetailsUiEvent.NavigateBack) },
    openShowDetails = { eventSink(ShowDetailsUiEvent.OpenShowDetails(it)) },
    openEpisodeDetails = { eventSink(ShowDetailsUiEvent.OpenEpisodeDetails(it)) },
    refresh = { eventSink(ShowDetailsUiEvent.Refresh(true)) },
    onMessageShown = { eventSink(ShowDetailsUiEvent.ClearMessage(it)) },
    openSeason = { eventSink(ShowDetailsUiEvent.OpenSeason(it)) },
    onSeasonFollowed = { eventSink(ShowDetailsUiEvent.FollowSeason(it)) },
    onSeasonUnfollowed = { eventSink(ShowDetailsUiEvent.UnfollowSeason(it)) },
    unfollowPreviousSeasons = { eventSink(ShowDetailsUiEvent.UnfollowPreviousSeasons(it)) },
    onMarkSeasonWatched = { seasonId, airedOnly ->
      eventSink(ShowDetailsUiEvent.MarkSeasonWatched(seasonId, onlyAired = airedOnly))
    },
    onMarkSeasonUnwatched = { eventSink(ShowDetailsUiEvent.MarkSeasonUnwatched(it)) },
    onToggleShowFollowed = { eventSink(ShowDetailsUiEvent.ToggleShowFollowed) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun ShowDetails(
  viewState: ShowDetailsUiState,
  navigateUp: () -> Unit,
  openShowDetails: (showId: Long) -> Unit,
  openEpisodeDetails: (episodeId: Long) -> Unit,
  refresh: () -> Unit,
  onMessageShown: (id: Long) -> Unit,
  openSeason: (seasonId: Long) -> Unit,
  onSeasonFollowed: (seasonId: Long) -> Unit,
  onSeasonUnfollowed: (seasonId: Long) -> Unit,
  unfollowPreviousSeasons: (seasonId: Long) -> Unit,
  onMarkSeasonWatched: (seasonId: Long, airedOnly: Boolean) -> Unit,
  onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
  onToggleShowFollowed: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  val dismissSnackbarState = rememberDismissState { value ->
    if (value != DismissValue.Default) {
      snackbarHostState.currentSnackbarData?.dismiss()
      true
    } else {
      false
    }
  }

  viewState.message?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
      // Notify the view model that the message has been dismissed
      onMessageShown(message.id)
    }
  }

  val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

  HazeScaffold(
    topBar = {
      ShowDetailsAppBar(
        title = null,
        isRefreshing = viewState.refreshing,
        onNavigateUp = navigateUp,
        onRefresh = refresh,
        scrollBehavior = scrollBehavior,
        modifier = Modifier
          .noIndicationClickable {
            coroutineScope.launch { listState.animateScrollToItem(0) }
          }
          .fillMaxWidth(),
      )
    },
    floatingActionButton = {
      val expanded by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
      }

      ToggleShowFollowFloatingActionButton(
        isFollowed = viewState.isFollowed,
        expanded = expanded,
        onClick = onToggleShowFollowed,
        modifier = Modifier
          .testTag("show_details_follow_button"),
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
    modifier = modifier
      .nestedScroll(scrollBehavior.nestedScrollConnection),
  ) { contentPadding ->
    Surface(modifier = Modifier.bodyWidth()) {
      ShowDetailsScrollingContent(
        show = viewState.show,
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
        modifier = Modifier
          .testTag("show_details_lazycolumn")
          .fillMaxSize(),
      )
    }
  }
}

@Composable
private fun ShowDetailsScrollingContent(
  show: TiviShow,
  relatedShows: List<RelatedShowEntryWithShow>,
  nextEpisodeToWatch: EpisodeWithSeason?,
  seasons: List<SeasonWithEpisodesAndWatches>,
  watchStats: ShowsWatchStats?,
  listState: LazyListState,
  openShowDetails: (showId: Long) -> Unit,
  openEpisodeDetails: (episodeId: Long) -> Unit,
  openSeason: (seasonId: Long) -> Unit,
  onSeasonFollowed: (seasonId: Long) -> Unit,
  onSeasonUnfollowed: (seasonId: Long) -> Unit,
  unfollowPreviousSeasons: (seasonId: Long) -> Unit,
  onMarkSeasonWatched: (seasonId: Long, airedOnly: Boolean) -> Unit,
  onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  val gutter = Layout.gutter
  val bodyMargin = Layout.bodyMargin

  LazyColumn(
    state = listState,
    contentPadding = contentPadding.copy(copyTop = false),
    modifier = modifier,
  ) {
    item(key = "backdrop") {
      Backdrop(
        imageModel = rememberShowImageModel(show, ImageType.BACKDROP),
        shape = RectangleShape,
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 11),
      )
    }

    item {
      val title = show.title?.takeIf { it.isNotEmpty() }
      if (title != null) {
        Text(
          text = title,
          style = MaterialTheme.typography.displaySmall,
          letterSpacing = (-1).sp,
          lineHeight = 36.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .padding(horizontal = bodyMargin, vertical = max(gutter, bodyMargin))
            .fillMaxWidth(),
        )
      }
    }

    item(key = "poster_info") {
      PosterInfoRow(
        show = show,
        modifier = Modifier.fillMaxWidth(),
      )
    }

    gutterSpacer()

    item(key = "header_summary") {
      Header(LocalStrings.current.detailsAbout)
    }

    if (show.summary != null) {
      item(key = "summary") {
        ExpandingText(
          text = show.summary!!,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
        )
      }
    }

    if (show.genres.isNotEmpty()) {
      item(key = "genres") {
        Genres(show.genres)
      }
    }

    if (nextEpisodeToWatch?.episode != null) {
      gutterSpacer()

      item(key = "header_next_episode_watch") {
        Header(LocalStrings.current.detailsNextEpisode)
      }
      item(key = "next_episode_watch") {
        NextEpisodeToWatch(
          season = nextEpisodeToWatch.season,
          episode = nextEpisodeToWatch.episode,
          onClick = { openEpisodeDetails(nextEpisodeToWatch.episode.id) },
        )
      }
    }

    if (relatedShows.isNotEmpty()) {
      gutterSpacer()

      item(key = "header_related_shows") {
        Header(LocalStrings.current.detailsRelated)
      }
      item(key = "related_shows") {
        RelatedShows(
          related = relatedShows,
          openShowDetails = openShowDetails,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }

    if (watchStats != null) {
      gutterSpacer()

      item(key = "header_watch_stats") {
        Header(LocalStrings.current.detailsViewStats)
      }
      item(key = "watch_stats") {
        WatchStats(watchStats.watchedEpisodeCount, watchStats.episodeCount)
      }
    }

    if (seasons.isNotEmpty()) {
      gutterSpacer()

      item(key = "header_seasons") {
        Header(LocalStrings.current.showDetailsSeasons)
      }

      items(
        items = seasons,
        key = { "season_${it.season.id}" },
      ) { season ->
        SeasonRow(
          season = season.season,
          episodesAired = season.numberAired,
          episodesWatched = season.numberWatched,
          episodesToWatch = season.numberAiredToWatch,
          episodesToAir = season.numberToAir,
          nextToAirDate = season.nextToAir?.firstAired,
          contentPadding = PaddingValues(horizontal = bodyMargin, vertical = gutter),
          openSeason = openSeason,
          onSeasonFollowed = onSeasonFollowed,
          onSeasonUnfollowed = onSeasonUnfollowed,
          unfollowPreviousSeasons = unfollowPreviousSeasons,
          onMarkSeasonWatched = onMarkSeasonWatched,
          onMarkSeasonUnwatched = onMarkSeasonUnwatched,
          modifier = Modifier
            .testTag("show_details_season_item")
            .fillParentMaxWidth(),
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
  modifier: Modifier = Modifier,
) {
  Row(modifier.padding(horizontal = Layout.bodyMargin)) {
    AsyncImage(
      model = rememberShowImageModel(show, ImageType.POSTER),
      contentDescription = LocalStrings.current.cdShowPosterImage(show.title ?: ""),
      modifier = Modifier
        .weight(1f)
        .aspectRatio(2 / 3f)
        .clip(MaterialTheme.shapes.medium),
      alignment = Alignment.TopStart,
    )

    Spacer(modifier = Modifier.width(Layout.gutter * 2))

    InfoPanels(
      show = show,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
private fun NetworkInfoPanel(
  networkName: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.networkTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = networkName,
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Composable
private fun RuntimeInfoPanel(
  runtime: Int,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.runtimeTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = LocalStrings.current.minutesFormat(runtime),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Composable
private fun ShowStatusPanel(
  showStatus: ShowStatus,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.statusTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    val textCreator = LocalTiviTextCreator.current
    Text(
      text = textCreator.showStatusText(showStatus).toString(),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Composable
private fun AirsInfoPanel(
  show: TiviShow,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.airsTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    val textCreator = LocalTiviTextCreator.current
    Text(
      text = textCreator.airsText(show).toString(),
      style = MaterialTheme.typography.bodyMedium,
    )
  }
}

@Composable
private fun CertificateInfoPanel(
  certification: String,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.certificateTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = certification,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier
        .border(
          width = 1.dp,
          color = MaterialTheme.colorScheme.onSurface,
          shape = RoundedCornerShape(2.dp),
        )
        .padding(horizontal = 4.dp, vertical = 2.dp),
    )
  }
}

@Composable
private fun TraktRatingInfoPanel(
  rating: Float,
  votes: Int,
  modifier: Modifier = Modifier,
) {
  Column(modifier) {
    Text(
      text = LocalStrings.current.traktRatingTitle,
      style = MaterialTheme.typography.titleSmall,
    )

    Spacer(Modifier.height(4.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Image(
        imageVector = Icons.Default.Star,
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.secondary),
        modifier = Modifier.size(32.dp),
      )

      Spacer(Modifier.width(4.dp))

      Column {
        Text(
          text = LocalStrings.current.traktRatingText(rating * 10f),
          style = MaterialTheme.typography.bodyMedium,
        )

        Text(
          text = LocalStrings.current.traktRatingVotes(votes / 1000f),
          style = MaterialTheme.typography.bodySmall,
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
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Genres(genres: List<Genre>) {
  val textCreator = LocalTiviTextCreator.current

  Box(
    Modifier
      .fillMaxWidth()
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
      .semantics {
        contentDescription = textCreator
          .genreContentDescription(genres)
          .toString()
      },
  ) {
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      genres.forEach { genre ->
        Card {
          Text(
            text = textCreator.genreLabel(genre),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RelatedShows(
  related: List<RelatedShowEntryWithShow>,
  openShowDetails: (showId: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()
  val contentPadding = PaddingValues(horizontal = Layout.bodyMargin, vertical = Layout.gutter)

  LazyRow(
    state = lazyListState,
    modifier = modifier,
    flingBehavior = rememberSnapFlingBehavior(lazyListState, SnapPositionInLayout.StartToStart),
    contentPadding = contentPadding,
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    items(
      items = related,
      key = { it.show.id },
    ) { item ->
      PosterCard(
        show = item.show,
        onClick = { openShowDetails(item.show.id) },
        modifier = Modifier
          .animateItemPlacement()
          .fillParentMaxWidth(0.21f) // 21% of the available width
          .aspectRatio(2 / 3f),
      )
    }
  }
}

@Composable
private fun NextEpisodeToWatch(
  season: Season,
  episode: Episode,
  onClick: () -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .heightIn(min = 48.dp)
      .wrapContentHeight()
      .clickable(onClick = onClick)
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
  ) {
    val textCreator = LocalTiviTextCreator.current

    Text(
      textCreator.seasonEpisodeTitleText(season, episode),
      style = MaterialTheme.typography.bodySmall,
    )

    Spacer(Modifier.height(4.dp))

    Text(
      text = episode.title ?: LocalStrings.current.episodeTitleFallback(episode.number!!),
      style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InfoPanels(
  show: TiviShow,
  modifier: Modifier = Modifier,
) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(Layout.gutter * 2),
    modifier = modifier,
  ) {
    val itemMod = Modifier.padding(bottom = Layout.gutter * 2)

    if (show.traktRating != null) {
      TraktRatingInfoPanel(
        rating = show.traktRating!!,
        votes = show.traktVotes ?: 0,
        modifier = itemMod,
      )
    }
    if (show.network != null) {
      NetworkInfoPanel(
        networkName = show.network!!,
        modifier = itemMod,
      )
    }
    if (show.status != null) {
      ShowStatusPanel(showStatus = show.status!!, modifier = itemMod)
    }
    if (show.certification != null) {
      CertificateInfoPanel(certification = show.certification!!, modifier = itemMod)
    }
    if (show.runtime != null) {
      RuntimeInfoPanel(runtime = show.runtime!!, modifier = itemMod)
    }
    if (show.airsDay != null &&
      show.airsTime != null &&
      show.airsTimeZone != null &&
      (show.status == ShowStatus.IN_PRODUCTION || show.status == ShowStatus.RETURNING)
    ) {
      AirsInfoPanel(show = show, modifier = itemMod)
    }
  }
}

@Composable
private fun WatchStats(
  watchedEpisodeCount: Int,
  episodeCount: Int,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter),
  ) {
    LinearProgressIndicator(
      progress = {
        when {
          episodeCount > 0 -> watchedEpisodeCount / episodeCount.toFloat()
          else -> 0f
        }
      },
      modifier = Modifier.fillMaxWidth(),
    )

    Spacer(modifier = Modifier.height(Layout.gutter))

    val textCreator = LocalTiviTextCreator.current

    // TODO: Do something better with CharSequences containing markup/spans
    Text(
      text = "${textCreator.followedShowEpisodeWatchStatus(watchedEpisodeCount, episodeCount)}",
      style = MaterialTheme.typography.bodyMedium,
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
  onMarkSeasonWatched: (seasonId: Long, airedOnly: Boolean) -> Unit,
  onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
  nextToAirDate: Instant? = null,
) {
  val contentColor = when {
    season.ignored -> LocalContentColor.current.copy(alpha = 0.4f)
    else -> LocalContentColor.current
  }

  CompositionLocalProvider(LocalContentColor provides contentColor) {
    Row(
      modifier = modifier
        .clip(MaterialTheme.shapes.medium)
        .clickable(enabled = !season.ignored) {
          openSeason(season.id)
        }
        .heightIn(min = 48.dp)
        .wrapContentHeight(Alignment.CenterVertically)
        .padding(contentPadding),
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .align(Alignment.CenterVertically),
      ) {
        val textCreator = LocalTiviTextCreator.current

        Text(
          text = season.title
            ?: LocalStrings.current.seasonTitleFallback(season.number!!),
          style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = textCreator.seasonSummaryText(
            watched = episodesWatched,
            toWatch = episodesToWatch,
            toAir = episodesToAir,
            nextToAirDate = nextToAirDate,
          ).toString(),
          style = MaterialTheme.typography.bodySmall,
        )

        if (!season.ignored && episodesAired > 0) {
          LinearProgressIndicator(
            progress = { episodesWatched / episodesAired.toFloat() },
            modifier = Modifier
              .padding(top = 4.dp)
              .fillMaxWidth(),
          )
        }
      }

      Box(modifier = Modifier.align(Alignment.CenterVertically)) {
        var showMenu by remember { mutableStateOf(false) }

        IconButton(onClick = { showMenu = true }) {
          Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = LocalStrings.current.cdOpenOverflow,
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
          onMarkSeasonUnwatched = onMarkSeasonUnwatched,
        )
      }
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
  onMarkSeasonWatched: (seasonId: Long, airedOnly: Boolean) -> Unit,
  onMarkSeasonUnwatched: (seasonId: Long) -> Unit,
) {
  DropdownMenu(
    expanded = expanded,
    onDismissRequest = onDismissRequest,
  ) {
    if (season.ignored) {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.popupSeasonFollow) },
        onClick = {
          onSeasonFollowed(season.id)
          onDismissRequest()
        },
      )
    } else {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.popupSeasonIgnore) },
        onClick = {
          onSeasonUnfollowed(season.id)
          onDismissRequest()
        },
      )
    }

    // Season number starts from 1, rather than 0
    if ((season.number ?: -100) >= 2) {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.popupSeasonIgnorePrevious) },
        onClick = {
          unfollowPreviousSeasons(season.id)
          onDismissRequest()
        },
      )
    }

    if (episodesWatched > 0) {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.popupSeasonMarkAllUnwatched) },
        onClick = {
          onMarkSeasonUnwatched(season.id)
          onDismissRequest()
        },
      )
    }

    if (episodesToAir > 0 && episodesWatched < episodesAired) {
      DropdownMenuItem(
        text = { Text(LocalStrings.current.popupSeasonMarkWatchedAired) },
        onClick = {
          onMarkSeasonWatched(season.id, true)
          onDismissRequest()
        },
      )
    }

    val episodeCount = episodesAired + episodesToAir

    if (episodesWatched < episodeCount) {
      DropdownMenuItem(
        text = { Text(text = LocalStrings.current.popupSeasonMarkWatchedAll) },
        onClick = {
          onMarkSeasonWatched(season.id, false)
          onDismissRequest()
        },
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowDetailsAppBar(
  title: String?,
  isRefreshing: Boolean,
  onNavigateUp: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier,
  scrollBehavior: TopAppBarScrollBehavior,
) {
  TopAppBar(
    title = {
      if (title != null) {
        Text(text = title)
      }
    },
    navigationIcon = {
      ScrimmedIconButton(
        showScrim = scrollBehavior.state.contentOffset > -4,
        onClick = onNavigateUp,
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBackForPlatform,
          contentDescription = LocalStrings.current.cdNavigateUp,
        )
      }
    },
    actions = {
      RefreshButton(
        showScrim = scrollBehavior.state.contentOffset > -4,
        refreshing = !isRefreshing,
        onClick = onRefresh,
      )
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
      scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
    ),
    scrollBehavior = scrollBehavior,
    modifier = modifier,
  )
}

@Composable
private fun ToggleShowFollowFloatingActionButton(
  isFollowed: Boolean,
  onClick: () -> Unit,
  expanded: Boolean,
  modifier: Modifier = Modifier,
) {
  ExtendedFloatingActionButton(
    onClick = onClick,
    icon = {
      Icon(
        imageVector = when {
          isFollowed -> Icons.Default.Favorite
          else -> Icons.Default.FavoriteBorder
        },
        contentDescription = when {
          isFollowed -> LocalStrings.current.cdFollowShowRemove
          else -> LocalStrings.current.cdFollowShowAdd
        },
      )
    },
    text = {
      Text(
        when {
          isFollowed -> LocalStrings.current.followShowRemove
          else -> LocalStrings.current.followShowAdd
        },
      )
    },
    containerColor = when {
      isFollowed -> FloatingActionButtonDefaults.containerColor
      else -> MaterialTheme.colorScheme.surface
    },
    expanded = expanded,
    modifier = modifier,
  )
}
