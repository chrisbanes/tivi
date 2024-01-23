// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.ui.minus
import app.tivi.common.compose.ui.plus

private val LocalScaffoldContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

/**
 * A copy of Material 3's [Scaffold] composable, but with a few tweaks:
 *
 * - Supports being used nested. The `contentPadding` is compounded on each level.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NestedScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  content: @Composable (PaddingValues) -> Unit,
) {
  val upstreamContentPadding = LocalScaffoldContentPadding.current
  val layoutDirection = LocalLayoutDirection.current

  val insets = remember {
    MutableWindowInsets(
      contentWindowInsets.add(PaddingValuesInsets(upstreamContentPadding)),
    )
  }

  LaunchedEffect(contentWindowInsets, upstreamContentPadding, layoutDirection) {
    insets.insets = contentWindowInsets.add(PaddingValuesInsets(upstreamContentPadding))
  }

  Scaffold(
    modifier = modifier,
    topBar = topBar,
    bottomBar = bottomBar,
    snackbarHost = snackbarHost,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    containerColor = containerColor,
    contentColor = contentColor,
    contentWindowInsets = insets,
  ) { contentPadding ->
    val contentPaddingMinusInsets = contentPadding.minus(
      contentWindowInsets.asPaddingValues(),
      layoutDirection,
    )

    CompositionLocalProvider(LocalScaffoldContentPadding provides contentPaddingMinusInsets) {
      content(contentPadding)
    }
  }
}

@Stable
private data class PaddingValuesInsets(private val paddingValues: PaddingValues) : WindowInsets {
  override fun getLeft(density: Density, layoutDirection: LayoutDirection) = with(density) {
    paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
  }

  override fun getTop(density: Density) = with(density) {
    paddingValues.calculateTopPadding().roundToPx()
  }

  override fun getRight(density: Density, layoutDirection: LayoutDirection) = with(density) {
    paddingValues.calculateRightPadding(layoutDirection).roundToPx()
  }

  override fun getBottom(density: Density) = with(density) {
    paddingValues.calculateBottomPadding().roundToPx()
  }
}
