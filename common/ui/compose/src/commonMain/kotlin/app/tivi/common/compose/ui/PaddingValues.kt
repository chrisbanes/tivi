// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.copy(
    copyStart: Boolean = true,
    copyTop: Boolean = true,
    copyEnd: Boolean = true,
    copyBottom: Boolean = true,
): PaddingValues {
    return remember(this) {
        derivedStateOf {
            PaddingValues(
                start = if (copyStart) calculateStartPadding(LayoutDirection.Ltr) else 0.dp,
                top = if (copyTop) calculateTopPadding() else 0.dp,
                end = if (copyEnd) calculateEndPadding(LayoutDirection.Ltr) else 0.dp,
                bottom = if (copyBottom) calculateBottomPadding() else 0.dp,
            )
        }
    }.value
}

operator fun PaddingValues.plus(plus: PaddingValues): PaddingValues = PaddingValues(
    start = this.calculateStartPadding(LayoutDirection.Ltr) +
        plus.calculateStartPadding(LayoutDirection.Ltr),
    top = this.calculateTopPadding() + plus.calculateTopPadding(),
    end = this.calculateEndPadding(LayoutDirection.Ltr) +
        plus.calculateEndPadding(LayoutDirection.Ltr),
    bottom = this.calculateBottomPadding() + plus.calculateBottomPadding(),
)
