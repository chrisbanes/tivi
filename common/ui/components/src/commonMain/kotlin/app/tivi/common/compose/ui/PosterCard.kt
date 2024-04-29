// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.Box
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
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TiviShow

@Composable
fun PosterCard(
  show: TiviShow,
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier) {
    PosterCardContent(show = show)
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosterCard(
  show: TiviShow,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(onClick = onClick, modifier = modifier) {
    PosterCardContent(show = show)
  }
}

@Composable
private fun PosterCardContent(show: TiviShow) {
  Box(modifier = Modifier.fillMaxSize()) {
    Text(
      text = show.title ?: "No title",
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier
        .padding(4.dp)
        .align(Alignment.CenterStart),
    )
    AsyncImage(
      model = show.asImageModel(ImageType.POSTER),
      contentDescription = LocalStrings.current.cdShowPosterImage(show.title ?: "show"),
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop,
    )
  }
}

@Composable
fun PlaceholderPosterCard(
  modifier: Modifier = Modifier,
) {
  Card(modifier = modifier) {
    Box {
      // TODO: display something better
    }
  }
}
