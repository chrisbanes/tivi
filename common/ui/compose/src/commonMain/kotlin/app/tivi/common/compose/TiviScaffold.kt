// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import app.tivi.common.compose.ui.plus
import dev.chrisbanes.haze.haze

private val LocalScaffoldContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

/**
 * A copy of Material 3's [Scaffold] composable, but with a few tweaks:
 *
 * - Supports being used nested. The `contentPadding` is compounded on each level.
 * - Supports automatic blurring of content over [topBar] and [bottomBar], via
 * [blurTopBar] and [blurBottomBar].
 */
@Composable
fun TiviScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  blurTopBar: Boolean = false,
  blurBottomBar: Boolean = false,
  content: @Composable (PaddingValues) -> Unit,
) {
  Surface(modifier = modifier, color = containerColor, contentColor = contentColor) {
    NestedScaffoldLayout(
      fabPosition = floatingActionButtonPosition,
      topBar = topBar,
      bottomBar = bottomBar,
      content = content,
      snackbar = snackbarHost,
      contentWindowInsets = contentWindowInsets,
      fab = floatingActionButton,
      blurTopBar = blurTopBar,
      blurBottomBar = blurBottomBar,
      incomingContentPadding = LocalScaffoldContentPadding.current,
    )
  }
}

/**
 * Layout for a [Scaffold]'s content.
 *
 * @param fabPosition [FabPosition] for the FAB (if present)
 * @param topBar the content to place at the top of the [Scaffold], typically a [SmallTopAppBar]
 * @param content the main 'body' of the [Scaffold]
 * @param snackbar the [Snackbar] displayed on top of the [content]
 * @param fab the [FloatingActionButton] displayed on top of the [content], below the [snackbar]
 * and above the [bottomBar]
 * @param bottomBar the content to place at the bottom of the [Scaffold], on top of the
 * [content], typically a [NavigationBar].
 */
@Composable
private fun NestedScaffoldLayout(
  fabPosition: FabPosition,
  topBar: @Composable () -> Unit,
  content: @Composable (PaddingValues) -> Unit,
  snackbar: @Composable () -> Unit,
  fab: @Composable () -> Unit,
  contentWindowInsets: WindowInsets,
  incomingContentPadding: PaddingValues,
  bottomBar: @Composable () -> Unit,
  blurTopBar: Boolean,
  blurBottomBar: Boolean,
) {
  SubcomposeLayout { constraints ->
    val layoutWidth = constraints.maxWidth
    val layoutHeight = constraints.maxHeight

    val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

    layout(layoutWidth, layoutHeight) {
      val topBarPlaceables = subcompose(ScaffoldLayoutContent.TopBar, topBar).map {
        it.measure(looseConstraints)
      }

      val contentInsets = incomingContentPadding + contentWindowInsets
        .asPaddingValues(this@SubcomposeLayout)
      val leftInset = contentInsets.calculateLeftPadding(layoutDirection).roundToPx()
      val rightInset = contentInsets.calculateRightPadding(layoutDirection).roundToPx()
      val bottomInset = contentInsets.calculateBottomPadding().roundToPx()

      val topBarHeight = topBarPlaceables.maxByOrNull { it.height }?.height ?: 0

      val snackbarPlaceables = subcompose(ScaffoldLayoutContent.Snackbar, snackbar).map {
        // respect only bottom and horizontal for snackbar and fab
        // offset the snackbar constraints by the insets values
        it.measure(
          looseConstraints.offset(-leftInset - rightInset, -bottomInset),
        )
      }

      val snackbarHeight = snackbarPlaceables.maxByOrNull { it.height }?.height ?: 0
      val snackbarWidth = snackbarPlaceables.maxByOrNull { it.width }?.width ?: 0

      val fabPlaceables = subcompose(ScaffoldLayoutContent.Fab, fab).mapNotNull { measurable ->
        measurable.measure(
          looseConstraints.offset(-leftInset - rightInset, -bottomInset),
        ).takeIf { it.height != 0 && it.width != 0 }
      }

      val fabPlacement = if (fabPlaceables.isNotEmpty()) {
        val fabWidth = fabPlaceables.maxByOrNull { it.width }!!.width
        val fabHeight = fabPlaceables.maxByOrNull { it.height }!!.height
        // FAB distance from the left of the layout, taking into account LTR / RTL
        val fabLeftOffset = if (fabPosition == FabPosition.End) {
          if (layoutDirection == LayoutDirection.Ltr) {
            layoutWidth - FabSpacing.roundToPx() - fabWidth
          } else {
            FabSpacing.roundToPx()
          }
        } else {
          (layoutWidth - fabWidth) / 2
        }

        FabPlacement(
          left = fabLeftOffset,
          width = fabWidth,
          height = fabHeight,
        )
      } else {
        null
      }

      val bottomBarPlaceables = subcompose(ScaffoldLayoutContent.BottomBar) {
        CompositionLocalProvider(
          LocalFabPlacement provides fabPlacement,
          content = bottomBar,
        )
      }.map { it.measure(looseConstraints) }

      val bottomBarHeight = bottomBarPlaceables.maxByOrNull { it.height }?.height
      val fabOffsetFromBottom = fabPlacement?.let {
        // Total height is the bottom bar height + the FAB height + the padding
        // between the FAB and bottom bar
        (bottomBarHeight ?: bottomInset) + it.height + FabSpacing.roundToPx()
      }

      val snackbarOffsetFromBottom = if (snackbarHeight != 0) {
        snackbarHeight + (fabOffsetFromBottom ?: bottomBarHeight ?: bottomInset)
      } else {
        0
      }

      val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
        val innerPadding = PaddingValues(
          top = if (topBarPlaceables.isEmpty()) {
            contentInsets.calculateTopPadding()
          } else {
            topBarHeight.toDp()
          },
          bottom = if (bottomBarPlaceables.isEmpty() || bottomBarHeight == null) {
            contentInsets.calculateBottomPadding()
          } else {
            bottomBarHeight.toDp()
          },
          start = contentInsets.calculateStartPadding((this@SubcomposeLayout).layoutDirection),
          end = contentInsets.calculateEndPadding((this@SubcomposeLayout).layoutDirection),
        )

        val blurAreas = listOfNotNull(
          if (blurTopBar) {
            Rect(
              left = 0f,
              top = 0f,
              right = layoutWidth.toFloat(),
              bottom = innerPadding.calculateTopPadding().toPx(),
            ).takeUnless { it.isEmpty }
          } else {
            null
          },
          if (blurBottomBar) {
            Rect(
              left = 0f,
              top = layoutHeight.toFloat() - innerPadding.calculateBottomPadding().toPx(),
              right = layoutWidth.toFloat(),
              bottom = layoutHeight.toFloat(),
            ).takeUnless { it.isEmpty }
          } else {
            null
          },
        )

        Box(
          modifier = Modifier.haze(
            *blurAreas.toTypedArray(),
            backgroundColor = MaterialTheme.colorScheme.surface,
          ),
        ) {
          // Scaffold always applies the insets, so we only want to pass down the content padding
          // without the insets (i.e. padding from the bottom bar, etc)
          CompositionLocalProvider(LocalScaffoldContentPadding provides innerPadding) {
            content(innerPadding)
          }
        }
      }.map { it.measure(looseConstraints) }

      // Placing to control drawing order to match default elevation of each placeable

      bodyContentPlaceables.forEach {
        it.place(0, 0)
      }
      topBarPlaceables.forEach {
        it.place(0, 0)
      }
      snackbarPlaceables.forEach {
        it.place(
          x = (layoutWidth - snackbarWidth) / 2 + leftInset,
          y = layoutHeight - snackbarOffsetFromBottom,
        )
      }
      // The bottom bar is always at the bottom of the layout
      bottomBarPlaceables.forEach {
        it.place(0, layoutHeight - (bottomBarHeight ?: 0))
      }
      // Explicitly not using placeRelative here as `leftOffset` already accounts for RTL
      fabPlacement?.let { placement ->
        fabPlaceables.forEach {
          it.place(placement.left, layoutHeight - fabOffsetFromBottom!!)
        }
      }
    }
  }
}

/**
 * Placement information for a [FloatingActionButton] inside a [Scaffold].
 *
 * @property left the FAB's offset from the left edge of the bottom bar, already adjusted for RTL
 * support
 * @property width the width of the FAB
 * @property height the height of the FAB
 */
@Immutable
internal class FabPlacement(
  val left: Int,
  val width: Int,
  val height: Int,
)

/**
 * CompositionLocal containing a [FabPlacement] that is used to calculate the FAB bottom offset.
 */
internal val LocalFabPlacement = staticCompositionLocalOf<app.tivi.common.compose.FabPlacement?> { null }

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp

private enum class ScaffoldLayoutContent { TopBar, MainContent, Snackbar, Fab, BottomBar }
