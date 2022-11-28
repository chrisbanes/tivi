/*
 * Copyright 2022 Google LLC
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import app.tivi.common.compose.Layout
import app.tivi.common.ui.resources.R

@Composable
fun Backdrop(
    imageModel: Any?,
    modifier: Modifier = Modifier,
    overline: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
) {
    Surface(
        color = MaterialTheme.colors.onSurface
            .copy(alpha = 0.2f)
            .compositeOver(MaterialTheme.colors.surface),
        contentColor = MaterialTheme.colors.onSurface,
        modifier = modifier,
    ) {
        Box {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawForegroundGradientScrim(
                        MaterialTheme.colors.background.copy(alpha = 0.9f),
                    ),
            ) {
                if (imageModel != null) {
                    AsyncImage(
                        model = imageModel,
                        requestBuilder = { crossfade(true) },
                        contentDescription = stringResource(R.string.cd_show_poster),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(Layout.gutter * 2),
            ) {
                if (overline != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.overline,
                        LocalContentAlpha provides ContentAlpha.medium,
                    ) {
                        overline()
                    }
                }
                if (title != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.h5,
                    ) {
                        title()
                    }
                }
            }
        }
    }
}
