// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.theme.TiviTheme

@Composable
fun ScrimmedIconButton(
  showScrim: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  invertThemeOnScrim: Boolean = true,
  icon: @Composable () -> Unit,
) {
  IconButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier,
  ) {
    if (invertThemeOnScrim) {
      val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5

      Crossfade(targetState = showScrim) { show ->
        TiviTheme(useDarkColors = if (show) isLight else !isLight) {
          ScrimSurface(showScrim = show, icon = icon)
        }
      }
    } else {
      ScrimSurface(showScrim = showScrim, icon = icon)
    }
  }
}

@Composable
private fun ScrimSurface(
  modifier: Modifier = Modifier,
  showScrim: Boolean = true,
  alpha: Float = 0.5f,
  icon: @Composable () -> Unit,
) {
  Surface(
    color = when {
      showScrim -> MaterialTheme.colorScheme.surface.copy(alpha = alpha)
      else -> Color.Transparent
    },
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = MaterialTheme.shapes.small,
    modifier = modifier,
    content = {
      Box(Modifier.padding(4.dp)) {
        icon()
      }
    },
  )
}
