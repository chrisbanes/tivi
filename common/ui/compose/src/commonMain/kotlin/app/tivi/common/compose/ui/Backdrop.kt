// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import app.tivi.common.compose.Layout
import app.tivi.common.compose.LocalStrings

@Composable
fun Backdrop(
  imageModel: Any?,
  modifier: Modifier = Modifier,
  shape: Shape = MaterialTheme.shapes.medium,
  overline: (@Composable () -> Unit)? = null,
  title: (@Composable () -> Unit)? = null,
) {
  Surface(
    color = MaterialTheme.colorScheme.onSurface
      .copy(alpha = 0.2f)
      .compositeOver(MaterialTheme.colorScheme.surface),
    contentColor = MaterialTheme.colorScheme.onSurface,
    shape = shape,
    modifier = modifier,
  ) {
    Box {
      if (imageModel != null) {
        val strings = LocalStrings.current

        AsyncImage(
          model = imageModel,
          contentDescription = strings.cdShowPoster,
          contentScale = ContentScale.Crop,
          modifier = Modifier
            .fillMaxSize()
            .let { mod ->
              if (title != null) {
                mod.drawForegroundGradientScrim(
                  MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                )
              } else {
                mod
              }
            },
        )
      }

      Column(
        Modifier
          .align(Alignment.BottomStart)
          .padding(Layout.gutter * 2),
      ) {
        if (overline != null) {
          CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.labelSmall,
          ) {
            overline()
          }
        }
        if (title != null) {
          CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.headlineSmall,
          ) {
            title()
          }
        }
      }
    }
  }
}
