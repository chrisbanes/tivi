// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.upnext

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.itemKey
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.ui.AsyncImage
import app.tivi.common.compose.ui.EmptyContent
import app.tivi.common.compose.ui.SortChip
import app.tivi.common.compose.ui.TiviRootScreenAppBar
import app.tivi.common.compose.ui.noIndicationClickable
import app.tivi.common.compose.ui.plus
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.Episode
import app.tivi.data.models.ImageType
import app.tivi.data.models.Season
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.navigation.LocalNavigator
import app.tivi.overlays.showInBottomSheet
import app.tivi.overlays.showInDialog
import app.tivi.screens.AccountScreen
import app.tivi.screens.EpisodeTrackScreen
import app.tivi.screens.UpNextScreen
import coil3.compose.AsyncImagePainter
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.coroutines.launch
import me.saket.swipe.SwipeAction
import me.saket.swipe.SwipeableActionsBox
import me.tatarka.inject.annotations.Inject

@Inject
class UpNextUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is UpNextScreen -> {
      ui<UpNextUiState> { state, modifier ->
        UpNext(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun UpNext(
  state: UpNextUiState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val overlayHost = LocalOverlayHost.current
  val navigator = LocalNavigator.current

  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  UpNext(
    state = state,
    openEpisodeDetails = { eventSink(UpNextUiEvent.OpenEpisodeDetails(it)) },
    openTrackEpisode = {
      scope.launch {
        overlayHost.showInBottomSheet(EpisodeTrackScreen(it))
      }
    },
    onMessageShown = { eventSink(UpNextUiEvent.ClearMessage(it)) },
    openUser = {
      scope.launch {
        overlayHost.showInDialog(AccountScreen, navigator::goTo)
      }
    },
    refresh = { eventSink(UpNextUiEvent.Refresh(fromUser = true)) },
    onSortSelected = { eventSink(UpNextUiEvent.ChangeSort(it)) },
    onToggleFollowedShowsOnly = { eventSink(UpNextUiEvent.ToggleFollowedShowsOnly) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun UpNext(
  state: UpNextUiState,
  openEpisodeDetails: (episodeId: Long) -> Unit,
  openTrackEpisode: (episodeId: Long) -> Unit,
  onMessageShown: (id: Long) -> Unit,
  refresh: () -> Unit,
  openUser: () -> Unit,
  onSortSelected: (SortOption) -> Unit,
  onToggleFollowedShowsOnly: () -> Unit,
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

  val coroutineScope = rememberCoroutineScope()
  val lazyGridState = rememberLazyGridState()

  HazeScaffold(
    topBar = {
      TiviRootScreenAppBar(
        title = LocalStrings.current.upnextTitle,
        loggedIn = state.authState == TraktAuthState.LOGGED_IN,
        user = state.user,
        refreshing = state.isLoading,
        onRefreshActionClick = refresh,
        onUserActionClick = openUser,
        modifier = Modifier
          .noIndicationClickable {
            coroutineScope.launch { lazyGridState.animateScrollToItem(0) }
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
    modifier = modifier.fillMaxSize(),
  ) { contentPadding ->
    val refreshState = rememberPullRefreshState(
      refreshing = state.isLoading,
      onRefresh = refresh,
    )
    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
      val columns = Layout.columns
      val bodyMargin = Layout.bodyMargin
      val gutter = Layout.gutter

      LazyVerticalGrid(
        state = lazyGridState,
        columns = GridCells.Fixed(columns / 4),
        contentPadding = contentPadding.plus(
          PaddingValues(
            horizontal = (bodyMargin - 8.dp).coerceAtLeast(0.dp),
            vertical = (gutter - 8.dp).coerceAtLeast(0.dp),
          ),
          LocalLayoutDirection.current,
        ),
        // We minus 8.dp off the grid padding, as we use content padding on the items below
        horizontalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
        verticalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
        modifier = Modifier
          .bodyWidth()
          .fillMaxHeight(),
      ) {
        fullSpanItem {
          UpNextFilterRow(
            followedShowsOnly = state.followedShowsOnly,
            onToggleFollowedShowsOnly = onToggleFollowedShowsOnly,
            availableSorts = state.availableSorts,
            currentSort = state.sort,
            onSortSelected = onSortSelected,
          )
        }

        if (state.items.itemCount == 0 && state.items.loadState.refresh != LoadState.Loading) {
          fullSpanItem {
            EmptyContent(
              title = { Text(text = LocalStrings.current.upnextEmptyTitle) },
              prompt = { Text(text = LocalStrings.current.upnextEmptyPrompt) },
              graphic = { Text(text = "\uD83D\uDC7B") },
              modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 64.dp),
            )
          }
        }

        items(
          count = state.items.itemCount,
          key = state.items.itemKey { it.show.id },
        ) { index ->
          val entry = state.items[index]
          if (entry != null) {
            SwipeUpNextItem(
              onSwipe = { openTrackEpisode(entry.episode.id) },
              modifier = Modifier
                .animateItemPlacement()
                .fillMaxWidth(),
            ) {
              UpNextItem(
                show = entry.show,
                season = entry.season,
                episode = entry.episode,
                onClick = { openEpisodeDetails(entry.episode.id) },
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.fillMaxWidth(),
              )
            }
          }
        }
      }

      PullRefreshIndicator(
        refreshing = state.isLoading,
        state = refreshState,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(contentPadding),
        scale = true,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun UpNextFilterRow(
  followedShowsOnly: Boolean,
  onToggleFollowedShowsOnly: () -> Unit,
  availableSorts: List<SortOption>,
  currentSort: SortOption,
  onSortSelected: (SortOption) -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
    modifier = modifier
      .padding(vertical = 8.dp, horizontal = 8.dp),
  ) {
    FilterChip(
      selected = followedShowsOnly,
      leadingIcon = {
        AnimatedVisibility(visible = followedShowsOnly) {
          Icon(
            imageVector = Icons.Default.Done,
            contentDescription = null,
          )
        }
      },
      onClick = onToggleFollowedShowsOnly,
      label = {
        Text(text = LocalStrings.current.upnextFilterFollowedShowsOnlyTitle)
      },
    )

    Spacer(modifier = Modifier.weight(1f))

    SortChip(
      sortOptions = availableSorts,
      currentSortOption = currentSort,
      onSortSelected = onSortSelected,
    )
  }
}

@Composable
private fun SwipeUpNextItem(
  onSwipe: () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable BoxScope.() -> Unit,
) {
  val trackEpisode = SwipeAction(
    icon = {
      Icon(
        imageVector = Icons.Outlined.Visibility,
        contentDescription = null, // decorative
        modifier = Modifier
          .padding(vertical = 16.dp, horizontal = 24.dp),
      )
    },
    background = MaterialTheme.colorScheme.secondary,
    onSwipe = onSwipe,
  )

  SwipeableActionsBox(
    endActions = listOf(trackEpisode),
    swipeThreshold = 80.dp, // icon + padding + 8.dp
    backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp),
    modifier = modifier,
    content = content,
  )
}

@Composable
private fun UpNextItem(
  show: TiviShow,
  episode: Episode,
  season: Season,
  onClick: () -> Unit,
  contentPadding: PaddingValues,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier
      .clip(MaterialTheme.shapes.medium)
      .clickable(onClick = onClick)
      .padding(contentPadding),
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth(0.3f) // 30% of the width
        .aspectRatio(16 / 11f),
    ) {
      var model: Any by remember { mutableStateOf(episode.asImageModel()) }

      AsyncImage(
        model = model,
        onState = { state ->
          if (state is AsyncImagePainter.State.Error && model is EpisodeImageModel) {
            // If the episode backdrop request failed, fallback to the show backdrop
            model = show.asImageModel(ImageType.BACKDROP)
          }
        },
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
      )
    }

    Spacer(Modifier.width(16.dp))

    Column {
      val textCreator = LocalTiviTextCreator.current

      Text(
        text = textCreator.showTitle(show = show).toString(),
        style = MaterialTheme.typography.titleMedium,
      )

      Text(
        text = textCreator.seasonEpisodeTitleText(season, episode),
        style = MaterialTheme.typography.bodySmall,
      )

      Text(
        text = episode.title ?: "",
        style = MaterialTheme.typography.bodySmall,
      )

      Spacer(Modifier.height(8.dp))
    }
  }
}
