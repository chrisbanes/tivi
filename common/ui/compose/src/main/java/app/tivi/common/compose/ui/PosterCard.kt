/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import app.tivi.common.ui.resources.MR
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TiviShow
import dev.icerock.moko.resources.compose.stringResource

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
            requestBuilder = { crossfade(true) },
            contentDescription = stringResource(
                MR.strings.cd_show_poster_image,
                show.title ?: "show",
            ),
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
