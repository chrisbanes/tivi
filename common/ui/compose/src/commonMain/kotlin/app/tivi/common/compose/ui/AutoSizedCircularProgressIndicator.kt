// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import kotlin.math.roundToInt

@Composable
fun AutoSizedCircularProgressIndicator(
  modifier: Modifier = Modifier,
  color: Color = MaterialTheme.colorScheme.primary,
) {
  BoxWithConstraints(modifier) {
    val diameter = with(LocalDensity.current) {
      // We need to minus the padding added within CircularProgressIndicator
      min(constraints.maxWidth.toDp(), constraints.maxHeight.toDp()) - InternalPadding
    }

    CircularProgressIndicator(
      strokeWidth = (diameter.value * StrokeDiameterFraction)
        .roundToInt().dp
        .coerceAtLeast(2.dp),
      color = color,
    )
  }
}

// Default stroke size
private val DefaultStrokeWidth = 4.dp

// Preferred diameter for CircularProgressIndicator
private val DefaultDiameter = 40.dp

// Internal padding added by CircularProgressIndicator
private val InternalPadding = 4.dp

private val StrokeDiameterFraction = DefaultStrokeWidth / DefaultDiameter
