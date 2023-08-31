// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.home

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import app.tivi.extensions.fluentIf

@Composable
internal fun PreviousContent(
    isVisible: () -> Boolean = { true },
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            // If we're not visible, don't measure, layout (or draw)
            .fluentIf(!isVisible()) { emptyLayout() }
            // Content in the back stack should not be interactive until they're on top
            .pointerInput(Unit) {},
    ) {
        content()
    }
}

/**
 * This no-ops measure + layout (and thus draw) for child content.
 */
private fun Modifier.emptyLayout(): Modifier = layout { _, constraints ->
    layout(constraints.minWidth, constraints.minHeight) {}
}
