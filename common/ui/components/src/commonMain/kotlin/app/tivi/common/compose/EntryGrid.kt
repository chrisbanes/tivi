// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DismissValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paging.LoadStateLoading
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import app.tivi.common.compose.ui.PosterCard
import app.tivi.common.compose.ui.RefreshButton
import app.tivi.common.compose.ui.TopAppBar
import app.tivi.common.compose.ui.plus
import app.tivi.data.compoundmodels.EntryWithShow
import app.tivi.data.models.Entry
import app.tivi.data.models.TiviShow
import app.tivi.data.models.TrendingShowEntry
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun <E : Entry> EntryGrid(
  lazyPagingItems: LazyPagingItems<EntryWithShow<E>>,
  title: String,
  onNavigateUp: () -> Unit,
  onOpenShowDetails: (Long) -> Unit,
  modifier: Modifier = Modifier,
) {
  val snackbarHostState = remember { SnackbarHostState() }

  val dismissSnackbarState = rememberDismissState(
    confirmValueChange = { value ->
      if (value != DismissValue.Default) {
        snackbarHostState.currentSnackbarData?.dismiss()
        true
      } else {
        false
      }
    }
  )

  lazyPagingItems.loadState.prependErrorOrNull()?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
    }
  }
  lazyPagingItems.loadState.appendErrorOrNull()?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
    }
  }
  lazyPagingItems.loadState.refreshErrorOrNull()?.let { message ->
    LaunchedEffect(message) {
      snackbarHostState.showSnackbar(message.message)
    }
  }

  HazeScaffold(
    topBar = {
      EntryGridAppBar(
        title = title,
        onNavigateUp = onNavigateUp,
        refreshing = lazyPagingItems.loadState.refresh == LoadStateLoading,
        onRefreshActionClick = { lazyPagingItems.refresh() },
        modifier = Modifier.fillMaxWidth(),
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
    val refreshing = lazyPagingItems.loadState.refresh == LoadStateLoading
    val refreshState = rememberPullRefreshState(
      refreshing = refreshing,
      onRefresh = lazyPagingItems::refresh,
    )
    Box(modifier = Modifier.pullRefresh(state = refreshState)) {
      val columns = Layout.columns
      val bodyMargin = Layout.bodyMargin
      val gutter = Layout.gutter

      LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed((columns / 2f).roundToInt()),
        contentPadding = paddingValues.plus(
          PaddingValues(horizontal = bodyMargin, vertical = gutter),
          LocalLayoutDirection.current,
        ),
        horizontalArrangement = Arrangement.spacedBy(gutter),
        verticalItemSpacing = gutter,
        modifier = Modifier
          .bodyWidth()
          .fillMaxHeight(),
      ) {
        items(
          count = lazyPagingItems.itemCount,
          key = lazyPagingItems.itemKey { it.show.id },
        ) { index ->
          val entry = lazyPagingItems[index]

          if (entry != null) {
            GridItem(
              show = entry.show,
              entry = entry.entry,
              onClick = { onOpenShowDetails(entry.show.id) },
              modifier = Modifier
                .animateItemPlacement()
                .fillMaxWidth(),
            )
          }
        }

        if (lazyPagingItems.loadState.append == LoadStateLoading) {
          item(span = StaggeredGridItemSpan.FullLine) {
            Box(
              Modifier
                .fillMaxWidth()
                .padding(24.dp),
            ) {
              CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
          }
        }
      }

      PullRefreshIndicator(
        refreshing = refreshing,
        state = refreshState,
        modifier = Modifier
          .align(Alignment.TopCenter)
          .padding(paddingValues),
        scale = true,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun <ET : Entry> GridItem(
  show: TiviShow,
  entry: ET,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    onClick = onClick,
  ) {
    Box(
      modifier = Modifier
        .aspectRatio(2 / 3f)
        .fillMaxWidth(),
    ) {
      PosterCard(
        show = show,
        modifier = Modifier.matchParentSize(),
      )
    }

    Column(
      modifier = Modifier.padding(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Text(
        text = show.title ?: "",
        style = MaterialTheme.typography.titleMedium,
        lineHeight = 18.sp,
        fontWeight = FontWeight.SemiBold,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
      ) {
        ProvideTextStyle(MaterialTheme.typography.labelMedium) {
          show.network?.let { network ->
            Text(text = network)
          }

          show.traktRating?.let { rating ->
            TextWithIcon(
              text = LocalStrings.current.traktRatingText(rating * 10),
              icon = Icons.Default.Star,
            )
          }

          if (entry is TrendingShowEntry) {
            TextWithIcon(
              text = entry.watchers.toString(),
              icon = Icons.Default.Visibility,
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EntryGridAppBar(
  title: String,
  refreshing: Boolean,
  onNavigateUp: () -> Unit,
  onRefreshActionClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  TopAppBar(
    navigationIcon = {
      IconButton(onClick = onNavigateUp) {
        Icon(
          imageVector = Icons.Default.ArrowBack,
          contentDescription = LocalStrings.current.cdNavigateUp,
        )
      }
    },
    modifier = modifier,
    transparent = true,
    title = { Text(text = title) },
    actions = {
      // This button refresh allows screen-readers, etc to trigger a refresh.
      // We only show the button to trigger a refresh, not to indicate that
      // we're currently refreshing, otherwise we have 4 indicators showing the
      // same thing.
      Crossfade(
        targetState = refreshing,
        modifier = Modifier.align(Alignment.CenterVertically),
      ) { isRefreshing ->
        if (!isRefreshing) {
          RefreshButton(onClick = onRefreshActionClick)
        }
      }
    },
  )
}

@Composable
private fun TextWithIcon(
  text: String,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(2.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier,
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(14.dp),
      )
    }

    Text(text = text)
  }
}
