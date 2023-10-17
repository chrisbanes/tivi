// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
internal actual fun colorScheme(
  useDarkColors: Boolean,
  useDynamicColors: Boolean,
): ColorScheme = when {
  useDarkColors -> TiviDarkColors
  else -> TiviLightColors
}
