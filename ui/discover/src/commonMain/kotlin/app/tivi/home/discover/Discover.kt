// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:OptIn(ExperimentalMaterial3Api::class)

package app.tivi.home.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.DynamicTheme
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.LocalWindowSizeClass
import app.tivi.common.compose.ReportDrawnWhen
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.theme.shouldUseDarkColors
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.AutoSizedCircularProgressIndicator
import app.tivi.common.compose.ui.BackdropCard
import app.tivi.common.compose.ui.ParallaxAlignment
import app.tivi.common.compose.ui.TiviRootScreenAppBar
import app.tivi.common.compose.ui.drawForegroundGradientScrim
import app.tivi.common.compose.ui.noIndicationClickable
import app.tivi.common.compose.ui.rememberShowImageModel
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.cd_show_poster_image
import app.tivi.common.ui.resources.details_next_episode
import app.tivi.common.ui.resources.discover_anticipated_title
import app.tivi.common.ui.resources.discover_popular_title
import app.tivi.common.ui.resources.discover_recommended_title
import app.tivi.common.ui.resources.discover_title
import app.tivi.common.ui.resources.discover_trending_title
import app.tivi.common.ui.resources.episode_title_fallback
import app.tivi.common.ui.resources.header_more
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.navigation.LocalNavigator
import app.tivi.overlays.showInDialog
import app.tivi.screens.AccountScreen
import app.tivi.screens.DiscoverScreen
import app.tivi.util.launchOrThrow
import coil3.compose.AsyncImagePainter
import com.materialkolor.PaletteStyle
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

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

  val navigator = LocalNavigator.current

  Discover(
    state = state,
    refresh = { eventSink(DiscoverUiEvent.Refresh(true)) },
    openUser = {
      scope.launchOrThrow {
        overlayHost.showInDialog(AccountScreen, navigator::goTo)
      }
    },
    openEpisodeDetails = { eventSink(DiscoverUiEvent.OpenEpisodeDetails(it)) },
    openShowDetails = { eventSink(DiscoverUiEvent.OpenShowDetails(it)) },
    openTrendingShows = { eventSink(DiscoverUiEvent.OpenTrendingShows) },
    openRecommendedShows = { eventSink(DiscoverUiEvent.OpenRecommendedShows) },
    openPopularShows = { eventSink(DiscoverUiEvent.OpenPopularShows) },
    openAnticipatedShows = { eventSink(DiscoverUiEvent.OpenAnticipatedShows) },
    onMessageShown = { eventSink(DiscoverUiEvent.ClearMessage(it)) },
    modifier = modifier,
  )
}

@OptIn(
  ExperimentalMaterialApi::class,
  ExperimentalFoundationApi::class,
  ExperimentalMaterial3Api::class,
)
@Composable
internal fun Discover(
  state: DiscoverUiState,
  refresh: () -> Unit,
  openUser: () -> Unit,
  openEpisodeDetails: (episodeId: Long) -> Unit,
  openShowDetails: (showId: Long) -> Unit,
  openTrendingShows: () -> Unit,
  openRecommendedShows: () -> Unit,
  openPopularShows: () -> Unit,
  openAnticipatedShows: () -> Unit,
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

  val coroutineScope = rememberCoroutineScope()
  val lazyListState = rememberLazyListState()

  HazeScaffold(
    topBar = {
      TiviRootScreenAppBar(
        title = stringResource(Res.string.discover_title),
        loggedIn = state.authState == TraktAuthState.LOGGED_IN,
        user = state.user,
        refreshing = state.refreshing,
        onRefreshActionClick = refresh,
        onUserActionClick = openUser,
        modifier = Modifier
          .noIndicationClickable {
            coroutineScope.launchOrThrow { lazyListState.animateScrollToItem(0) }
          }
          .fillMaxWidth(),
      )
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
    modifier = modifier,
  ) { paddingValues ->
    val refreshState = rememberPullRefreshState(refreshing = state.refreshing, onRefresh = refresh)
    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
      LazyColumn(
        contentPadding = paddingValues,
        state = lazyListState,
        modifier = Modifier.bodyWidth(),
      ) {
        item {
          Spacer(Modifier.height(Layout.gutter))
        }

        item(key = "carousel_next_to_watch") {
          val carouselState = rememberLazyListState()
          LazyRow(
            state = carouselState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
              horizontal = Layout.bodyMargin,
              vertical = Layout.gutter,
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            flingBehavior = rememberSnapFlingBehavior(carouselState, SnapPosition.Start),
          ) {
            items(
              items = state.nextEpisodesToWatch,
              key = { it.episode.id },
            ) { item ->
              NextEpisodeToWatchCard(
                show = item.show,
                season = item.season,
                episode = item.episode,
                onClick = { openEpisodeDetails(item.episode.id) },
                modifier = Modifier
                  .animateItem()
                  .width(260.dp),
              )
            }
          }
        }

        item(key = "carousel_trending") {
          CarouselWithHeader(
            items = state.trendingItems,
            tagPrefix = "trending",
            title = stringResource(Res.string.discover_trending_title),
            refreshing = state.trendingRefreshing,
            onItemClick = { openShowDetails(it.id) },
            onMoreClick = openTrendingShows,
            modifier = Modifier.animateItem(),
          )
        }

        item(key = "carousel_popular") {
          CarouselWithHeader(
            items = state.popularItems,
            tagPrefix = "popular",
            title = stringResource(Res.string.discover_popular_title),
            refreshing = state.popularRefreshing,
            onItemClick = { openShowDetails(it.id) },
            onMoreClick = openPopularShows,
            modifier = Modifier.animateItem(),
          )
        }

        item(key = "carousel_anticipated") {
          CarouselWithHeader(
            items = state.anticipatedItems,
            tagPrefix = "anticipated",
            title = stringResource(Res.string.discover_anticipated_title),
            refreshing = state.anticipatedRefreshing,
            onItemClick = { openShowDetails(it.id) },
            onMoreClick = openAnticipatedShows,
            modifier = Modifier.animateItem(),
          )
        }

        item(key = "carousel_recommended") {
          CarouselWithHeader(
            items = state.recommendedItems,
            tagPrefix = "recommended",
            title = stringResource(Res.string.discover_recommended_title),
            refreshing = state.recommendedRefreshing,
            onItemClick = { openShowDetails(it.id) },
            onMoreClick = openRecommendedShows,
            modifier = Modifier.animateItem(),
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

@Composable
private fun NextEpisodeToWatchCard(
  show: TiviShow,
  season: Season,
  episode: Episode,
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  DynamicTheme(
    model = rememberShowImageModel(show, ImageType.POSTER),
    useDarkTheme = shouldUseDarkColors(),
    fallback = MaterialTheme.colorScheme.surfaceVariant,
    style = PaletteStyle.TonalSpot,
  ) {
    Card(onClick = onClick, modifier = modifier) {
      Box {
        var model: Any by remember(episode.id) { mutableStateOf(episode.asImageModel()) }

        AsyncImage(
          model = model,
          onState = { state ->
            if (state is AsyncImagePainter.State.Error && model is EpisodeImageModel) {
              // If the episode backdrop request failed, fallback to the show backdrop
              model = show.asImageModel(ImageType.BACKDROP)
            }
          },
          contentDescription = null,
          modifier = Modifier
            .drawForegroundGradientScrim(MaterialTheme.colorScheme.surfaceVariant, decay = 2f)
            .fillMaxWidth()
            .aspectRatio(16 / 11f),
          contentScale = ContentScale.Crop,
        )

        Card(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .align(Alignment.BottomStart)
            .width(40.dp)
            .aspectRatio(10 / 16f),
        ) {
          AsyncImage(
            model = rememberShowImageModel(show, ImageType.POSTER),
            contentDescription = stringResource(Res.string.cd_show_poster_image, show.title ?: "show"),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
          )
        }
      }

      val textCreator = LocalTiviTextCreator.current

      Column(
        Modifier
          .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
      ) {
        Spacer(Modifier.height(6.dp))

        Text(
          text = stringResource(Res.string.details_next_episode) +
            " - " +
            textCreator.seasonEpisodeLabel(season.number!!, episode.number!!),
          style = MaterialTheme.typography.labelMedium,
          color = LocalContentColor.current.copy(alpha = 0.7f),
        )

        Text(
          text = episode.title ?: stringResource(Res.string.episode_title_fallback, episode.number!!),
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
        )

        if (episode.summary != null) {
          Text(
            text = episode.summary!!,
            style = MaterialTheme.typography.bodySmall,
            overflow = TextOverflow.Ellipsis,
            minLines = 2,
            maxLines = 2,
          )
        }
      }
    }
  }
}

@Composable
private fun <T : EntryWithShow<*>> CarouselWithHeader(
  items: List<T>,
  title: String,
  tagPrefix: String,
  refreshing: Boolean,
  onItemClick: (TiviShow) -> Unit,
  onMoreClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier) {
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
          Text(text = stringResource(Res.string.header_more))
        }
      }
    }
    if (items.isNotEmpty()) {
      EntryShowCarousel(
        items = items,
        tagPrefix = tagPrefix,
        onItemClick = onItemClick,
        modifier = Modifier
          .testTag("${tagPrefix}_carousel")
          .fillMaxWidth(),
      )
    }
    // TODO empty state
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun <T : EntryWithShow<*>> EntryShowCarousel(
  items: List<T>,
  tagPrefix: String,
  onItemClick: (TiviShow) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lazyListState = rememberLazyListState()

  LazyRow(
    state = lazyListState,
    modifier = modifier
      .padding(horizontal = Layout.bodyMargin, vertical = Layout.gutter)
      .clip(MaterialTheme.shapes.extraLarge),
    flingBehavior = rememberSnapFlingBehavior(lazyListState, SnapPosition.Start),
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
              val itemInfo =
                layoutInfo.visibleItemsInfo.firstOrNull { it.key == item.show.id }
                  ?: return@ParallaxAlignment 0f

              val adjustedOffset = itemInfo.offset - layoutInfo.viewportStartOffset
              (adjustedOffset / itemInfo.size.toFloat()).coerceIn(-1f, 1f)
            },
          )
        },
        modifier = Modifier
          .testTag("${tagPrefix}_carousel_item")
          .animateItem()
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
