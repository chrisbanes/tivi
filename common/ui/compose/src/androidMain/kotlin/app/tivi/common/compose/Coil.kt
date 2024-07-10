// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.toBitmap

@OptIn(ExperimentalCoilApi::class)
internal actual fun Image.toComposeImageBitmap(): ImageBitmap = toBitmap().asImageBitmap()
