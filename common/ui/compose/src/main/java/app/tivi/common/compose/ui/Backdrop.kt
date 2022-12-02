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
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
    title: (@Composable () -> Unit)? = null
) {
    Surface(
        color = MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.2f)
            .compositeOver(MaterialTheme.colorScheme.surface),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
    ) {
        Box {
            if (imageModel != null) {
                AsyncImage(
                    model = imageModel,
                    requestBuilder = { crossfade(true) },
                    contentDescription = stringResource(R.string.cd_show_poster),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .let { mod ->
                            if (title != null) {
                                mod.drawForegroundGradientScrim(
                                    MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                                )
                            } else {
                                mod
                            }
                        }
                )
            }

            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(Layout.gutter * 2)
            ) {
                if (overline != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.labelSmall
                    ) {
                        overline()
                    }
                }
                if (title != null) {
                    CompositionLocalProvider(
                        LocalTextStyle provides MaterialTheme.typography.headlineSmall
                    ) {
                        title()
                    }
                }
            }
        }
    }
}
