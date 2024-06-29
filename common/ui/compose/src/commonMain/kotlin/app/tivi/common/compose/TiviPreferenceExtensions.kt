// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import app.tivi.settings.Preference
import app.tivi.settings.TiviPreferences

@Composable
fun TiviPreferences.shouldUseDarkColors(): Boolean {
  val themePreference = theme.flow.collectAsState(initial = TiviPreferences.Theme.SYSTEM)

  return when (themePreference.value) {
    TiviPreferences.Theme.LIGHT -> false
    TiviPreferences.Theme.DARK -> true
    else -> isSystemInDarkTheme()
  }
}

@Composable
fun TiviPreferences.shouldUseDynamicColors(): Boolean {
  return useDynamicColors.flow.collectAsState(initial = true).value
}

@Composable
inline fun <T> Preference<T>.collectAsState() = flow.collectAsState(defaultValue)
