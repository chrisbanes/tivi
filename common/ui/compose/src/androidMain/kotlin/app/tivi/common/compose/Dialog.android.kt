// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable

@Composable
actual fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties,
    content: @Composable () -> Unit,
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = properties.dismissOnBackPress,
            dismissOnClickOutside = properties.dismissOnClickOutside,
            usePlatformDefaultWidth = properties.usePlatformDefaultWidth,
            decorFitsSystemWindows = properties.decorFitsSystemWindows,
        ),
        content = content,
    )
}
