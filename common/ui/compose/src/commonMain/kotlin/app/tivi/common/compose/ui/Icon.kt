// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector
import app.tivi.extensions.unsafeLazy

expect val Icons.AutoMirrored.Filled.ArrowBackForPlatform: ImageVector

internal val ArrowBackIosFixed: ImageVector by unsafeLazy {
  materialIcon(name = "AutoMirrored.Filled.ArrowBackIos", autoMirror = true) {
    val xOffset = 4.5f

    materialPath {
      moveTo(xOffset + 11.67f, 3.87f)
      lineTo(xOffset + 9.9f, 2.1f)
      lineTo(xOffset + 0.0f, 12.0f)
      lineToRelative(9.9f, 9.9f)
      lineToRelative(1.77f, -1.77f)
      lineTo(xOffset + 3.54f, 12.0f)
      close()
    }
  }
}
