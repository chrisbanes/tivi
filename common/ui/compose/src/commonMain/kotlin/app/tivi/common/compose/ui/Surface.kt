// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
@NonRestartableComposable
fun Surface(
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  shape: Shape = RectangleShape,
  color: Color = MaterialTheme.colorScheme.surface,
  contentColor: Color = contentColorFor(color),
  tonalElevation: Dp = 0.dp,
  shadowElevation: Dp = 0.dp,
  border: BorderStroke? = null,
  interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  content: @Composable () -> Unit,
) {
  if (onClick != null) {
    androidx.compose.material3.Surface(
      onClick = onClick,
      modifier = modifier,
      enabled = enabled,
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = tonalElevation,
      shadowElevation = shadowElevation,
      border = border,
      interactionSource = interactionSource,
      content = content,
    )
  } else {
    androidx.compose.material3.Surface(
      modifier = modifier,
      shape = shape,
      color = color,
      contentColor = contentColor,
      tonalElevation = tonalElevation,
      shadowElevation = shadowElevation,
      border = border,
      content = content,
    )
  }
}
