// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import io.github.alexzhirkevich.cupertino.CupertinoScaffoldDefaults
import io.github.alexzhirkevich.cupertino.FabPosition
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme

@Composable
fun HazeScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = CupertinoTheme.colorScheme.systemBackground,
  contentColor: Color = CupertinoTheme.colorScheme.label,
  contentWindowInsets: WindowInsets = CupertinoScaffoldDefaults.contentWindowInsets,
  blurTopBar: Boolean = false,
  blurBottomBar: Boolean = false,
  content: @Composable (PaddingValues) -> Unit,
) {
  val hazeState = remember { HazeState() }

  NestedScaffold(
    modifier = modifier,
    topBar = {
      if (blurTopBar) {
        // We explicitly only want to add a Box if we are blurring.
        // Scaffold has logic which changes based on whether `bottomBar` contains a layout node.
        Box(Modifier.hazeChild(hazeState)) {
          topBar()
        }
      } else {
        topBar()
      }
    },
    bottomBar = {
      if (blurBottomBar) {
        // We explicitly only want to add a Box if we are blurring.
        // Scaffold has logic which changes based on whether `bottomBar` contains a layout node.
        Box(Modifier.hazeChild(hazeState)) {
          bottomBar()
        }
      } else {
        bottomBar()
      }
    },
    snackbarHost = snackbarHost,
    floatingActionButton = floatingActionButton,
    floatingActionButtonPosition = floatingActionButtonPosition,
    containerColor = containerColor,
    contentColor = contentColor,
    contentWindowInsets = contentWindowInsets,
  ) { contentPadding ->
    Box(
      modifier = Modifier.haze(
        state = hazeState,
        backgroundColor = containerColor,
      ),
    ) {
      content(contentPadding)
    }
  }
}
