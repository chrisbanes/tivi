// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.material3.Scaffold
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
import io.github.alexzhirkevich.cupertino.CupertinoScaffoldDefaults
import io.github.alexzhirkevich.cupertino.FabPosition
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveScaffold
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme

private val LocalScaffoldContentPadding = staticCompositionLocalOf { PaddingValues(0.dp) }

/**
 * A wrapper around [Scaffold] composable, but with a few tweaks:
 *
 * - Supports being used nested. The `contentPadding` is compounded on each level.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalAdaptiveApi::class)
@Composable
fun NestedScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = CupertinoTheme.colorScheme.systemBackground,
  contentColor: Color = CupertinoTheme.colorScheme.label,
  contentWindowInsets: WindowInsets = CupertinoScaffoldDefaults.contentWindowInsets,
  content: @Composable (PaddingValues) -> Unit
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

  AdaptiveScaffold(
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
