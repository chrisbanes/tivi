// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.Theme

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
        typography = TiviTypography,
        shapes = TiviShapes,
        content = it,
      )
    },
    content = content,
  )
}
