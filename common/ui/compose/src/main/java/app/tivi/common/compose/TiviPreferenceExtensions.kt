// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import app.tivi.settings.TiviPreferences

@Composable
fun TiviPreferences.shouldUseDarkColors(): Boolean {
    val themePreference = remember { observeTheme() }.collectAsState(initial = theme)
    return when (themePreference.value) {
        TiviPreferences.Theme.LIGHT -> false
        TiviPreferences.Theme.DARK -> true
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun TiviPreferences.shouldUseDynamicColors(): Boolean {
    return remember { observeUseDynamicColors() }
        .collectAsState(initial = useDynamicColors)
        .value
}
