// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import app.tivi.common.compose.LocalPreferences
import app.tivi.common.compose.collectAsState
import app.tivi.settings.TiviPreferences

@Composable
fun TiviTheme(
  useDarkColors: Boolean = shouldUseDarkColors(),
  useDynamicColors: Boolean = shouldUseDynamicColors(),
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = colorScheme(useDarkColors, useDynamicColors),
    typography = TiviTypography,
    shapes = TiviShapes,
    content = content,
  )
}

@Composable
fun shouldUseDarkColors(): Boolean {
  val themePreference = LocalPreferences.current.theme.collectAsState()
  return when (themePreference.value) {
    TiviPreferences.Theme.LIGHT -> false
    TiviPreferences.Theme.DARK -> true
    else -> isSystemInDarkTheme()
  }
}

@Composable
fun shouldUseDynamicColors(): Boolean {
  val state by LocalPreferences.current.useDynamicColors.collectAsState()
  return state
}
