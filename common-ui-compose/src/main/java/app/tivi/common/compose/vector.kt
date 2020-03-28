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
import androidx.ui.core.Modifier
import androidx.ui.foundation.Box
import androidx.ui.foundation.contentColor
import androidx.ui.graphics.Color
import androidx.ui.graphics.vector.drawVector
import androidx.ui.layout.LayoutSize
import androidx.ui.res.vectorResource

@Composable
fun VectorImage(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier.None,
    tintColor: Color = contentColor()
) {
    val vector = vectorResource(id)
    Box(
        modifier = modifier +
            LayoutSize(vector.defaultWidth, vector.defaultHeight) +
            drawVector(vectorImage = vector, tintColor = tintColor)
    )
}

@Composable
fun drawVectorResource(
    @DrawableRes id: Int,
    tintColor: Color = contentColor()
) = drawVector(vectorImage = vectorResource(id), tintColor = tintColor)
