// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.LocalStrings
import app.tivi.common.compose.theme.TiviTheme
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TiviShow

@Composable
fun BackdropCard(
  show: TiviShow,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
) {
  Card(
    shape = MaterialTheme.shapes.extraLarge,
    modifier = modifier,
  ) {
    TiviTheme(useDarkColors = true) {
      BackdropCardContent(
        show = show,
        alignment = alignment,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackdropCard(
  show: TiviShow,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  alignment: Alignment = Alignment.Center,
) {
  Card(
    onClick = onClick,
    shape = MaterialTheme.shapes.extraLarge,
    modifier = modifier,
  ) {
    TiviTheme(useDarkColors = true) {
      BackdropCardContent(
        show = show,
        alignment = alignment,
      )
    }
  }
}

@Composable
private fun BackdropCardContent(
  show: TiviShow,
  alignment: Alignment = Alignment.Center,
) {
  Box(modifier = Modifier.fillMaxSize()) {
    AsyncImage(
      model = show.asImageModel(ImageType.BACKDROP),
      contentDescription = LocalStrings.current.cdShowPosterImage(show.title ?: "show"),
      modifier = Modifier.matchParentSize(),
      contentScale = ContentScale.Crop,
      alignment = alignment,
    )

    Spacer(
      Modifier.matchParentSize()
        .drawForegroundGradientScrim(MaterialTheme.colorScheme.surface),
    )

    Text(
      text = show.title ?: "No title",
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier
        .padding(16.dp)
        .align(Alignment.BottomStart),
    )
  }
}

@Composable
fun PlaceholderBackdropCard(
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier) {
    Box {
      // TODO: display something better
    }
  }
}
