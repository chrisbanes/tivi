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
import androidx.compose.Composable
import androidx.compose.foundation.Box
import androidx.compose.foundation.contentColor
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.ui.core.Alignment
import androidx.ui.core.ContentScale
import androidx.ui.core.Modifier
import androidx.ui.core.clipToBounds
import androidx.ui.core.paint
import androidx.ui.res.vectorResource

@Composable
fun VectorImage(
    @DrawableRes id: Int,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    tintColor: Color = contentColor(),
    modifier: Modifier = Modifier.wrapContentSize(align = alignment)
) {
    VectorImage(
        vector = vectorResource(id = id),
        alignment = alignment,
        contentScale = contentScale,
        tintColor = tintColor,
        modifier = modifier
    )
}

@Composable
fun VectorImage(
    vector: VectorAsset,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Inside,
    tintColor: Color = contentColor(),
    modifier: Modifier = Modifier.wrapContentSize(align = alignment)
) {
    Box(
        modifier = modifier.clipToBounds().paint(
            painter = VectorPainter(vector),
            alignment = alignment,
            contentScale = contentScale,
            colorFilter = ColorFilter.tint(tintColor)
        )
    )
}
