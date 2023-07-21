// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit,
) {
    DialogWindow(
        onCloseRequest = onDismissRequest,
        state = rememberDialogState(size = properties.size),
        title = properties.title,
        icon = properties.icon,
        resizable = properties.resizable,
    ) {
        content()
    }
}
