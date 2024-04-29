// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun HazeScaffold(
  modifier: Modifier = Modifier,
  topBar: @Composable () -> Unit = {},
  bottomBar: @Composable () -> Unit = {},
  snackbarHost: @Composable () -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.End,
  containerColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = contentColorFor(containerColor),
  contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
  hazeState: HazeState = remember { HazeState() },
  blurTopBar: Boolean = false,
  blurBottomBar: Boolean = false,
  content: @Composable (PaddingValues) -> Unit,
) {
  NestedScaffold(
    modifier = modifier,
    topBar = {
      if (blurTopBar) {
        // We explicitly only want to add a Box if we are blurring.
        // Scaffold has logic which changes based on whether `bottomBar` contains a layout node.
        Box(
          modifier = Modifier
            .hazeChild(state = hazeState, style = HazeMaterials.regular()),
        ) {
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
        Box(
          modifier = Modifier
            .hazeChild(state = hazeState, style = HazeMaterials.regular()),
        ) {
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
    Box(Modifier.haze(state = hazeState, style = HazeDefaults.style(noiseFactor = 0f))) {
      content(contentPadding)
    }
  }
}
