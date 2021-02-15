/*
 * Copyright 2020 Google LLC
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

package app.tivi.common.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tivi.data.entities.TiviShow
import app.tivi.data.entities.TmdbImageEntity
import dev.chrisbanes.accompanist.coil.CoilImage

@Composable
fun PosterCard(
    show: TiviShow,
    modifier: Modifier = Modifier,
    poster: TmdbImageEntity? = null,
    onClick: (() -> Unit)? = null,
) {
    Card(modifier = modifier) {
        Box(
            modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
        ) {
            // TODO: remove text if the image has loaded (and animated in).
            // https://github.com/chrisbanes/accompanist/issues/76
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    text = show.title ?: "No title",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier
                        .padding(4.dp)
                        .align(Alignment.CenterStart)
                )
            }
            if (poster != null) {
                CoilImage(
                    data = poster,
                    contentDescription = stringResource(
                        R.string.cd_show_poster_image,
                        show.title ?: "show"
                    ),
                    fadeIn = true,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
fun PlaceholderPosterCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Box {
            // TODO: display something better
        }
    }
}
