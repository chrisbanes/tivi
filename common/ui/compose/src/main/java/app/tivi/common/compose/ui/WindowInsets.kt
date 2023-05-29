// Copyright 2022, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 *
 */
val WindowInsets.Companion.none: WindowInsets get() = EmptyWindowInsets

private object EmptyWindowInsets : WindowInsets {
    override fun getBottom(density: Density): Int = 0
    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int = 0
    override fun getTop(density: Density): Int = 0
}
