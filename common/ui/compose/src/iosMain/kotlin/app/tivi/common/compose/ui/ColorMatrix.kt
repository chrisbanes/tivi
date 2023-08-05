// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.ui.graphics.ColorMatrix

actual fun ColorMatrix.setBrightness(value: Float): ColorMatrix {
    this[0, 4] = value
    this[1, 4] = value
    this[2, 4] = value
    return this
}
