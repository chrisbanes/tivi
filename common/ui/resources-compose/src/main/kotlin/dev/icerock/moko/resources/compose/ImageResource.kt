// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package dev.icerock.moko.resources.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import dev.icerock.moko.resources.ImageResource

@Composable
fun painterResource(imageResource: ImageResource): Painter {
    return androidx.compose.ui.res.painterResource(id = imageResource.drawableResId)
}
