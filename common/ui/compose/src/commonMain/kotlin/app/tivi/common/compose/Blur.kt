// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color

expect fun Modifier.glassBlur(
  areas: List<Rect>,
  color: Color,
  blurRadius: Float = 48f,
): Modifier
