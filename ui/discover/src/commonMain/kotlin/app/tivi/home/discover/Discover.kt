// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.home.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.compose.ReportDrawnWhen
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.rememberCoroutineScope
import app.tivi.common.compose.rememberTiviFlingBehavior
import app.tivi.common.compose.rememberTiviSnapFlingBehavior
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.BackdropCard
import app.tivi.common.compose.ui.ParallaxAlignment
import app.tivi.common.compose.ui.TiviRootScreenAppBar
import app.tivi.common.compose.ui.drawForegroundGradientScrim
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.overlays.showInDialog
import app.tivi.screens.AccountScreen
import app.tivi.screens.DiscoverScreen
import com.seiko.imageloader.model.ImageResult
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class DiscoverUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is DiscoverScreen -> {
      ui<DiscoverUiState> { state, modifier ->
        Discover(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun Discover(
  state: DiscoverUiState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val overlayHost = LocalOverlayHost.current

  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  Discover(
    state = state,
    refresh = { eventSink(DiscoverUiEvent.Refresh(true)) },
    openUser = {
      scope.launch {
        overlayHost.showInDialog(AccountScreen)
      }
    },
    openShowDetails = { showId, seasonId, episodeId ->
      eventSink(DiscoverUiEvent.OpenShowDetails(showId, seasonId, episodeId))
    },
    openTrendingShows = { eventSink(DiscoverUiEvent.OpenTrendingShows) },
    openRecommendedShows = { eventSink(DiscoverUiEvent.OpenRecommendedShows) },
    openPopularShows = { eventSink(DiscoverUiEvent.OpenPopularShows) },
    onMessageShown = { eventSink(DiscoverUiEvent.ClearMessage(it)) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun Discover(
  state: DiscoverUiState,
  refresh: () -> Unit,
  openUser: () -> Unit,
  openShowDetails: (showId: Long, seasonId: Long?, episodeId: Long?) -> Unit,
  openTrendingShows: () -> Unit,
  openRecommendedShows: () -> Unit,
  openPopularShows: () -> Unit,
  onMessageShown: (id: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

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

  ReportDrawnWhen {
    !state.popularRefreshing &&
      !state.trendingRefreshing &&
      state.popularItems.isNotEmpty() &&
      state.trendingItems.isNotEmpty()
  }

  Scaffold(
    topBar = {
      TiviRootScreenAppBar(
        title = LocalStrings.current.discoverTitle,
        loggedIn = state.authState == TraktAuthState.LOGGED_IN,
        user = state.user,
        refreshing = state.refreshing,
        onRefreshActionClick = refresh,
        onUserActionClick = openUser,
        modifier = Modifier.fillMaxWidth(),
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
    modifier = modifier,
  ) { paddingValues ->
    val refreshState = rememberPullRefreshState(refreshing = false, onRefresh = refresh)
    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
      LazyColumn(
        contentPadding = paddingValues,
        flingBehavior = rememberTiviFlingBehavior(),
        modifier = Modifier.bodyWidth(),
      ) {
        item {
          Spacer(Modifier.height(Layout.gutter))
        }

        state.nextEpisodeWithShowToWatch?.let { nextEpisodeToWatch ->
          item(key = "next_episode_to_watch") {
            NextEpisodeToWatch(
              show = nextEpisodeToWatch.show,
              season = nextEpisodeToWatch.season,
              episode = nextEpisodeToWatch.episode,
              onClick = {
                openShowDetails(
                  nextEpisodeToWatch.show.id,
                  nextEpisodeToWatch.episode.seasonId,
                  nextEpisodeToWatch.episode.id,
                )
              },
              modifier = Modifier
                .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
                .animateItemPlacement()
                .fillMaxWidth()
                .aspectRatio(16 / 9f),
            )
          }

          item {
            Spacer(Modifier.height(Layout.gutter))
          }
        }

        item(key = "carousel_trending") {
          CarouselWithHeader(
            items = state.trendingItems,
            title = LocalStrings.current.discoverTrendingTitle,
            refreshing = state.trendingRefreshing,
            onItemClick = {
              openShowDetails(it.id, null, null)
            },
            onMoreClick = openTrendingShows,
            modifier = Modifier.animateItemPlacement(),
          )
        }

        item(key = "carousel_recommended") {
          CarouselWithHeader(
            items = state.recommendedItems,
            title = LocalStrings.current.discoverRecommendedTitle,
            refreshing = state.recommendedRefreshing,
            onItemClick = {
              openShowDetails(it.id, null, null)
            },
            onMoreClick = openRecommendedShows,
            modifier = Modifier.animateItemPlacement(),
          )
        }

        item(key = "carousel_refreshing") {
          CarouselWithHeader(
            items = state.popularItems,
            title = LocalStrings.current.discoverPopularTitle,
            refreshing = state.popularRefreshing,
            onItemClick = { openShowDetails(it.id, null, null) },
            onMoreClick = openPopularShows,
            modifier = Modifier.animateItemPlacement(),
          )
        }

        item {
          Spacer(Modifier.height(Layout.gutter))
        }
      }

      PullRefreshIndicator(
        refreshing = state.refreshing,
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
private fun NextEpisodeToWatch(
  show: TiviShow,
  season: Season,
  episode: Episode,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  TiviTheme(useDarkColors = true) {
    OutlinedCard(
      onClick = onClick,
      modifier = modifier,
      border = BorderStroke(3.dp, MaterialTheme.colorScheme.outline),
    ) {
      Box {
        var model: Any by remember { mutableStateOf(episode.asImageModel()) }

        AsyncImage(
          model = model,
          onAction = { state ->
            if (state is ImageResult.Error && model is EpisodeImageModel) {
              // If the episode backdrop request failed, fallback to the show backdrop
              model = show.asImageModel(ImageType.BACKDROP)
            }
          },
          contentDescription = null,
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop,
        )

        Spacer(
          Modifier.matchParentSize()
            .drawForegroundGradientScrim(
              color = MaterialTheme.colorScheme.surface,
              decay = 1.5f,
            ),
        )

        Column(
          Modifier
            .padding(Layout.gutter),
        ) {
          Card {
            Text(
              text = LocalStrings.current.discoverKeepWatchingTitle,
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier
                .padding(horizontal = Layout.gutter, vertical = 4.dp),
            )
          }

          Spacer(Modifier.weight(1f))

          Column(
            Modifier
              .padding(Layout.gutter),
          ) {
            Text(
              text = show.title.orEmpty(),
              style = MaterialTheme.typography.labelLarge,
              modifier = Modifier.padding(1.dp),
            )

            val textCreator = LocalTiviTextCreator.current
            Text(
              text = textCreator.seasonEpisodeTitleText(season, episode),
              style = MaterialTheme.typography.labelMedium,
            )

            Text(
              text = episode.title
                ?: LocalStrings.current.episodeTitleFallback(episode.number!!),
              style = MaterialTheme.typography.titleLarge,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun <T : EntryWithShow<*>> CarouselWithHeader(
  items: List<T>,
  title: String,
  refreshing: Boolean,
  onItemClick: (TiviShow) -> Unit,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier
      .testTag("discover_carousel"),
  ) {
    if (refreshing || items.isNotEmpty()) {
      Spacer(Modifier.height(Layout.gutter))

      Header(
        title = title,
        loading = refreshing,
        modifier = Modifier
          .padding(horizontal = Layout.bodyMargin)
          .fillMaxWidth(),
      ) {
        TextButton(
          onClick = onMoreClick,
          colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.secondary,
          ),
          modifier = Modifier.alignBy(FirstBaseline),
        ) {
          Text(text = LocalStrings.current.headerMore)
        }
      }
    }
    if (items.isNotEmpty()) {
      EntryShowCarousel(
        items = items,
        onItemClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
      )
    }
    // TODO empty state
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
  items: List<T>,
  onItemClick: (TiviShow) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()

  LazyRow(
    state = lazyListState,
    modifier = modifier
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
      .clip(MaterialTheme.shapes.extraLarge),
    flingBehavior = rememberTiviSnapFlingBehavior(
      snapLayoutInfoProvider = remember(lazyListState) {
        SnapLayoutInfoProvider(
          lazyListState = lazyListState,
          positionInLayout = { _, _, _ -> 0 }, // start
        )
      },
    ),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    items(
      items = items,
      key = { it.show.id },
    ) { item ->
      BackdropCard(
        show = item.show,
        onClick = { onItemClick(item.show) },
        alignment = remember {
          ParallaxAlignment(
            horizontalBias = {
              val layoutInfo = lazyListState.layoutInfo
              val itemInfo = layoutInfo.visibleItemsInfo.first {
                it.key == item.show.id
              }

              val adjustedOffset = itemInfo.offset - layoutInfo.viewportStartOffset
              (adjustedOffset / itemInfo.size.toFloat()).coerceIn(-1f, 1f)
            },
          )
        },
        modifier = Modifier
          .testTag("discover_carousel_item")
          .animateItemPlacement()
          .width(
            when (LocalWindowSizeClass.current.widthSizeClass) {
              WindowWidthSizeClass.Expanded -> 320.dp
              else -> 240.dp
            },
          )
          .aspectRatio(16 / 11f),
      )
    }
  }
}

@Composable
private fun Header(
  title: String,
  modifier: Modifier = Modifier,
  loading: Boolean = false,
  content: @Composable RowScope.() -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
    )

    Spacer(Modifier.weight(1f))

    AnimatedVisibility(visible = loading) {
      AutoSizedCircularProgressIndicator(
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.size(16.dp),
      )
    }

    content()
  }
}

// @Preview
@Composable
private fun PreviewHeader() {
  Surface(Modifier.fillMaxWidth()) {
    Header(
      title = "Being watched now",
      loading = true,
    )
  }
}
