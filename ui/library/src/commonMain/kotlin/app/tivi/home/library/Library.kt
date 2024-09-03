// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import app.tivi.common.compose.HazeScaffold
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalTiviDateFormatter
import app.tivi.common.compose.LocalTiviTextCreator
import app.tivi.common.compose.bodyWidth
import app.tivi.common.compose.fullSpanItem
import app.tivi.common.compose.ui.EmptyContent
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.SearchTextField
import app.tivi.common.compose.ui.SortChip
import app.tivi.common.compose.ui.TiviRootScreenAppBar
import app.tivi.common.compose.ui.noIndicationClickable
import app.tivi.common.compose.ui.plus
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.filter_shows
import app.tivi.common.ui.resources.library_empty_prompt
import app.tivi.common.ui.resources.library_empty_title
import app.tivi.common.ui.resources.library_last_watched
import app.tivi.common.ui.resources.library_title
import app.tivi.common.ui.resources.upnext_filter_followed_shows_only_title
import app.tivi.data.compoundmodels.LibraryShow
import app.tivi.data.models.SortOption
import app.tivi.data.models.TiviShow
import app.tivi.data.traktauth.TraktAuthState
import app.tivi.navigation.LocalNavigator
import app.tivi.overlays.showInDialog
import app.tivi.screens.AccountScreen
import app.tivi.screens.LibraryScreen
import app.tivi.util.launchOrThrow
import com.slack.circuit.overlay.LocalOverlayHost
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.Ui
import com.slack.circuit.runtime.ui.ui
import kotlinx.datetime.Instant
import me.tatarka.inject.annotations.Inject
import org.jetbrains.compose.resources.stringResource

@Inject
class LibraryUiFactory : Ui.Factory {
  override fun create(screen: Screen, context: CircuitContext): Ui<*>? = when (screen) {
    is LibraryScreen -> {
      ui<LibraryUiState> { state, modifier ->
        Library(state, modifier)
      }
    }

    else -> null
  }
}

@Composable
internal fun Library(
  state: LibraryUiState,
  modifier: Modifier = Modifier,
) {
  val scope = rememberCoroutineScope()
  val overlayHost = LocalOverlayHost.current
  val navigator = LocalNavigator.current

  // Need to extract the eventSink out to a local val, so that the Compose Compiler
  // treats it as stable. See: https://issuetracker.google.com/issues/256100927
  val eventSink = state.eventSink

  Library(
    state = state,
    openShowDetails = { eventSink(LibraryUiEvent.OpenShowDetails(it)) },
    onMessageShown = { eventSink(LibraryUiEvent.ClearMessage(it)) },
    onToggleIncludeFollowedShows = { eventSink(LibraryUiEvent.ToggleFollowedShowsIncluded) },
    openUser = {
      scope.launchOrThrow {
        overlayHost.showInDialog(AccountScreen, navigator::goTo)
      }
    },
    refresh = { eventSink(LibraryUiEvent.Refresh(true)) },
    onFilterChanged = { eventSink(LibraryUiEvent.ChangeFilter(it)) },
    onSortSelected = { eventSink(LibraryUiEvent.ChangeSort(it)) },
    modifier = modifier,
  )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
internal fun Library(
  state: LibraryUiState,
  openShowDetails: (showId: Long) -> Unit,
  onMessageShown: (id: Long) -> Unit,
  onToggleIncludeFollowedShows: () -> Unit,
  refresh: () -> Unit,
  openUser: () -> Unit,
  onFilterChanged: (String) -> Unit,
  onSortSelected: (SortOption) -> Unit,
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
        title = stringResource(Res.string.library_title),
        loggedIn = state.authState == TraktAuthState.LOGGED_IN,
        user = state.user,
        refreshing = state.isLoading,
        onRefreshActionClick = refresh,
        onUserActionClick = openUser,
        modifier = Modifier
          .noIndicationClickable {
            coroutineScope.launchOrThrow { lazyGridState.animateScrollToItem(0) }
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
  ) { paddingValues ->
    val refreshState = rememberPullRefreshState(
      refreshing = state.isLoading,
      onRefresh = refresh,
    )
    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
      LibraryGrid(
        state = state,
        lazyGridState = lazyGridState,
        lazyPagingItems = state.items,
        paddingValues = paddingValues,
        onFilterChanged = onFilterChanged,
        onToggleIncludeFollowedShows = onToggleIncludeFollowedShows,
        onSortSelected = onSortSelected,
        openShowDetails = openShowDetails,
        modifier = Modifier
          .bodyWidth()
          .fillMaxHeight(),
      )

      PullRefreshIndicator(
        refreshing = state.isLoading,
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
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun LibraryGrid(
  state: LibraryUiState,
  lazyGridState: LazyGridState,
  lazyPagingItems: LazyPagingItems<LibraryShow>,
  paddingValues: PaddingValues,
  onFilterChanged: (String) -> Unit,
  onToggleIncludeFollowedShows: () -> Unit,
  onSortSelected: (SortOption) -> Unit,
  openShowDetails: (showId: Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val columns = Layout.columns
  val bodyMargin = Layout.bodyMargin
  val gutter = Layout.gutter

  var filterExpanded by remember { mutableStateOf(false) }

  LazyVerticalGrid(
    state = lazyGridState,
    columns = GridCells.Fixed(columns / 4),
    contentPadding = paddingValues.plus(
      PaddingValues(
        horizontal = (bodyMargin - 8.dp).coerceAtLeast(0.dp),
        vertical = (gutter - 8.dp).coerceAtLeast(0.dp),
      ),
      LocalLayoutDirection.current,
    ),
    // We minus 8.dp off the grid padding, as we use content padding on the items below
    horizontalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
    verticalArrangement = Arrangement.spacedBy((gutter - 8.dp).coerceAtLeast(0.dp)),
    modifier = modifier,
  ) {
    fullSpanItem {
      var filter by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(state.filter ?: ""))
      }

      FilterSortPanel(
        filterIcon = {
          IconButton(onClick = { filterExpanded = true }) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null, // FIXME
            )
          }
        },
        filterTextField = {
          SearchTextField(
            value = filter,
            onValueChange = { value ->
              filter = value
              onFilterChanged(value.text)
            },
            hint = stringResource(Res.string.filter_shows, lazyPagingItems.itemCount),
            modifier = Modifier.fillMaxWidth(),
            onCleared = {
              filter = TextFieldValue()
              onFilterChanged("")
              filterExpanded = false
            },
          )
        },
        filterExpanded = filterExpanded,
        modifier = Modifier.padding(vertical = 8.dp),
      ) {
        FilterChip(
          selected = state.onlyFollowedShows,
          leadingIcon = {
            AnimatedVisibility(visible = state.onlyFollowedShows) {
              Icon(
                imageVector = Icons.Default.Done,
                contentDescription = null,
              )
            }
          },
          onClick = onToggleIncludeFollowedShows,
          label = {
            Text(text = stringResource(Res.string.upnext_filter_followed_shows_only_title))
          },
        )

        SortChip(
          sortOptions = state.availableSorts,
          currentSortOption = state.sort,
          onSortSelected = onSortSelected,
        )
      }
    }

    fullSpanItem {
      if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh != LoadState.Loading) {
        EmptyContent(
          title = { Text(text = stringResource(Res.string.library_empty_title)) },
          prompt = { Text(text = stringResource(Res.string.library_empty_prompt)) },
          graphic = { Text(text = "\uD83D\uDCFC") },
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
        LibraryItem(
          show = entry.show,
          watchedEpisodeCount = entry.stats?.watchedEpisodeCount,
          totalEpisodeCount = entry.stats?.episodeCount,
          lastWatchedDate = entry.watchedEntry?.lastWatched,
          onClick = { openShowDetails(entry.show.id) },
          contentPadding = PaddingValues(8.dp),
          modifier = Modifier
            .animateItemPlacement()
            .fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun FilterSortPanel(
  filterExpanded: Boolean,
  filterIcon: @Composable () -> Unit,
  filterTextField: @Composable () -> Unit,
  modifier: Modifier = Modifier,
  content: @Composable RowScope.() -> Unit,
) {
  Column(modifier = modifier) {
    Row(
      modifier = Modifier
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      AnimatedVisibility(!filterExpanded) {
        filterIcon()
      }

      content()
    }

    AnimatedVisibility(visible = filterExpanded) {
      filterTextField()
    }
  }
}

@Composable
private fun LibraryItem(
  show: TiviShow,
  watchedEpisodeCount: Int?,
  totalEpisodeCount: Int?,
  lastWatchedDate: Instant?,
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
    PosterCard(
      show = show,
      modifier = Modifier
        .fillMaxWidth(0.2f) // 20% of the width
        .aspectRatio(2 / 3f),
    )

    Spacer(Modifier.width(16.dp))

    Column {
      val textCreator = LocalTiviTextCreator.current

      Text(
        text = textCreator.showTitle(show = show).toString(),
        style = MaterialTheme.typography.titleMedium,
      )

      Spacer(Modifier.height(4.dp))

      if (watchedEpisodeCount != null && totalEpisodeCount != null) {
        LinearProgressIndicator(
          progress = {
            when {
              totalEpisodeCount > 0 -> watchedEpisodeCount / totalEpisodeCount.toFloat()
              else -> 0f
            }
          },
          modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(4.dp))

        Text(
          text = textCreator.followedShowEpisodeWatchStatus(
            episodeCount = totalEpisodeCount,
            watchedEpisodeCount = watchedEpisodeCount,
          ),
          style = MaterialTheme.typography.bodySmall,
        )
      } else if (lastWatchedDate != null) {
        Text(
          text = stringResource(
            Res.string.library_last_watched,
            LocalTiviDateFormatter.current.formatShortRelativeTime(lastWatchedDate),
          ),
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Spacer(Modifier.height(8.dp))
    }
  }
}
