// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun TiviTheme(
  useDarkColors: Boolean = isSystemInDarkTheme(),
  useDynamicColors: Boolean = false,
  content: @Composable () -> Unit,
) {
  AdaptiveTheme(
    target = Theme.Cupertino,
    material = {
      MaterialTheme(
        colorScheme = colorScheme(useDarkColors, useDynamicColors),
        typography = TiviMaterialTypography,
        shapes = TiviShapes,
        content = it,
      )
    },
    cupertino = {
      CupertinoTheme(
        typography = TiviCupertinoTypography,
        colorScheme = when {
            useDarkColors -> io.github.alexzhirkevich.cupertino.theme.darkColorScheme()
            else -> io.github.alexzhirkevich.cupertino.theme.lightColorScheme()
        },
        content = it,
      )
    },
    content = content,
  )
}
