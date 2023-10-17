// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
actual fun font(fontName: String, resourceId: String, weight: FontWeight, style: FontStyle): Font {
  return androidx.compose.ui.text.platform.Font("font/$resourceId.ttf", weight, style)
}
