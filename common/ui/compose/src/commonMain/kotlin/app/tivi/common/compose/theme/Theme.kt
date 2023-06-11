// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun TiviTheme(
    useDarkColors: Boolean = isSystemInDarkTheme(),
    useDynamicColors: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = when {
            Build.VERSION.SDK_INT >= 31 && useDynamicColors && useDarkColors -> {
                dynamicDarkColorScheme(LocalContext.current)
            }
            Build.VERSION.SDK_INT >= 31 && useDynamicColors && !useDarkColors -> {
                dynamicLightColorScheme(LocalContext.current)
            }
            useDarkColors -> TiviDarkColors
            else -> TiviLightColors
        },
        typography = TiviTypography,
        shapes = TiviShapes,
        content = content,
    )
}
