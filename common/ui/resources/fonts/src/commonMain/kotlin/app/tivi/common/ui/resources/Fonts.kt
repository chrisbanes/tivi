// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.ui.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
expect fun font(
  fontName: String,
  resourceId: String,
  weight: FontWeight,
  style: FontStyle = FontStyle.Normal,
): Font

val DmSansFontFamily: FontFamily
  @Composable get() = FontFamily(
    font(fontName = "DM Sans", resourceId = "dm_sans_regular", weight = FontWeight.Normal),
    font(fontName = "DM Sans", resourceId = "dm_sans_medium", weight = FontWeight.Medium),
    font(fontName = "DM Sans", resourceId = "dm_sans_bold", weight = FontWeight.Bold),
  )
