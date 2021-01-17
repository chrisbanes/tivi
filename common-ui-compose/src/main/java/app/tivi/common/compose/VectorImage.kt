/*
 * Copyright 2019 Google LLC
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

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadVectorResource

@Composable
fun VectorImage(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    tintColor: Color = foregroundColor(),
) {
    val deferred = loadVectorResource(id)
    deferred.onLoadRun { asset ->
        VectorImage(
            vector = asset,
            alignment = alignment,
            contentScale = contentScale,
            tintColor = tintColor,
            modifier = modifier
        )
    }
}

@Composable
fun VectorImage(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    tintColor: Color = foregroundColor(),
) {
    Box(
        modifier = modifier.clipToBounds().paint(
            painter = rememberVectorPainter(vector),
            alignment = alignment,
            contentScale = contentScale,
            colorFilter = ColorFilter.tint(tintColor)
        )
    )
}
