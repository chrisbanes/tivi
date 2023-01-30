// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.DpSize

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
val LocalWindowSizeClass = staticCompositionLocalOf {
    WindowSizeClass.calculateFromSize(DpSize.Zero)
}
