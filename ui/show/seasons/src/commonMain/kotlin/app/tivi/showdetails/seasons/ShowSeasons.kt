// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.showdetails.seasons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.ui.ArrowBackForPlatform
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.ExpandingText
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.noIndicationClickable
import app.tivi.data.compoundmodels.EpisodeWithWatches
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.Season
import app.tivi.screens.ShowSeasonsScreen
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class ShowSeasonsUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is ShowSeasonsScreen -> {
      ui<ShowSeasonsUiState> { state, modifier ->
        ShowSeasons(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun ShowSeasons(
  state: ShowSeasonsUiState,
  modifier: Modifier = Modifier,
) {
  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  ShowSeasons(
    state = state,
    navigateUp = { eventSink(ShowSeasonsUiEvent.NavigateBack) },
    openEpisodeDetails = { eventSink(ShowSeasonsUiEvent.OpenEpisodeDetails(it)) },
    refresh = { eventSink(ShowSeasonsUiEvent.Refresh(fromUser = true)) },
    onMessageShown = { eventSink(ShowSeasonsUiEvent.ClearMessage(it)) },
    modifier = modifier,
  )
}

@OptIn(
  ExperimentalFoundationApi::class,
  ExperimentalMaterialApi::class,
  ExperimentalMaterial3Api::class,
)
@Composable
internal fun ShowSeasons(
  state: ShowSeasonsUiState,
  navigateUp: () -> Unit,
  openEpisodeDetails: (episodeId: Long) -> Unit,
  refresh: () -> Unit,
  onMessageShown: (id: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }
  val coroutineScope = rememberCoroutineScope()
  val lazyListStates = remember { HashMap<Long, LazyListState>() }

  val dismissSnackbarState = rememberDismissState { value ->
    if (value != DismissValue.Default) {
      snackbarHostState.currentSnackbarData?.dismiss()
      true
    } else {
      false
    }
  }

  state.message?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
      // Notify the view model that the message has been dismissed
      onMessageShown(message.id)
    }
  }

  val pagerState = rememberPagerState(
    initialPage = 0,
    pageCount = { state.seasons.size },
  )

  var pagerBeenScrolled by remember { mutableStateOf(false) }
  LaunchedEffect(pagerState.isScrollInProgress) {
    if (pagerState.isScrollInProgress) pagerBeenScrolled = true
  }

  if (state.initialSeasonId != null && !pagerBeenScrolled && pagerState.canScrollForward) {
    val initialIndex = state.seasons.indexOfFirst { it.season.id == state.initialSeasonId }
    LaunchedEffect(initialIndex) {
      pagerState.scrollToPage(initialIndex)
    }
  }

  HazeScaffold(
    topBar = {
      Column {
        TopAppBar(
          title = { Text(text = state.show.title.orEmpty()) },
          navigationIcon = {
            IconButton(onClick = navigateUp) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBackForPlatform,
                contentDescription = LocalStrings.current.cdNavigateUp,
              )
            }
          },
          actions = {
            RefreshButton(
              refreshing = state.refreshing,
              onClick = refresh,
            )
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
          modifier = Modifier.noIndicationClickable {
            val currentSeasonId = state.seasons[pagerState.currentPage].season.id
            lazyListStates[currentSeasonId]?.let { lazyListState ->
              coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
              }
            }
          },
        )

        SeasonPagerTabs(
          pagerState = pagerState,
          seasons = state.seasons.map { it.season },
          modifier = Modifier.fillMaxWidth(),
          containerColor = Color.Transparent,
          contentColor = LocalContentColor.current,
        )

        HorizontalDivider(Modifier.fillMaxWidth())
      }
    },
    blurTopBar = true,
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
      .testTag("show_seasons")
      .fillMaxSize(),
  ) { contentPadding ->
    HorizontalPager(
      state = pagerState,
      flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
      modifier = Modifier
        .fillMaxHeight()
        .bodyWidth(),
    ) { page ->
      val seasonWithEps = state.seasons.getOrNull(page) ?: return@HorizontalPager
      SeasonPage(
        lazyListState = lazyListStates.getOrPut(seasonWithEps.season.id) {
          rememberLazyListState()
        },
        season = seasonWithEps.season,
        episodes = seasonWithEps.episodes,
        onEpisodeClick = openEpisodeDetails,
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize(),
      )
    }
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
      SecondaryIndicator(
        modifier = Modifier
          .pagerTabIndicatorOffset(pagerState, tabPositions),
      )
    },
    divider = {},
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

@Composable
private fun SeasonPage(
  season: Season,
  episodes: List<EpisodeWithWatches>,
  lazyListState: LazyListState,
  onEpisodeClick: (episodeId: Long) -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    state = lazyListState,
    modifier = modifier,
    contentPadding = contentPadding,
  ) {
    item {
      SeasonInfoRow(
        season = season,
        modifier = Modifier.fillParentMaxWidth(),
      )
    }

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
private fun SeasonInfoRow(
  season: Season,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier,
    tonalElevation = 2.dp,
  ) {
    Row(Modifier.padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)) {
      Card(
        modifier = Modifier
          .fillMaxWidth(0.25f)
          .aspectRatio(2 / 3f),
      ) {
        AsyncImage(
          model = remember(season, season::asImageModel),
          contentDescription = null,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      }

      Spacer(Modifier.width(Layout.gutter * 2))

      Column(Modifier.align(Alignment.CenterVertically)) {
        val textCreator = LocalTiviTextCreator.current
        Text(
          text = textCreator.seasonTitle(season),
          style = MaterialTheme.typography.titleMedium,
        )
        if (!season.summary.isNullOrEmpty()) {
          ExpandingText(
            text = season.summary.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            collapsedMaxLines = 6,
          )
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
          ?: LocalStrings.current.episodeTitleFallback(episode.number!!),
        style = MaterialTheme.typography.bodyMedium,
      )
    }

    var needSpacer = false
    if (hasPending) {
      Icon(
        imageVector = Icons.Default.CloudUpload,
        contentDescription = LocalStrings.current.cdEpisodeSyncing,
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
          onlyPendingDeletes -> LocalStrings.current.cdEpisodeDeleted
          else -> LocalStrings.current.cdEpisodeWatched
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
