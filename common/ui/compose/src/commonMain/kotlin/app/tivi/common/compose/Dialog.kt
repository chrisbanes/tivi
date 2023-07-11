// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
expect fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
)

@Immutable
data class DialogProperties(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val usePlatformDefaultWidth: Boolean = false,
    val decorFitsSystemWindows: Boolean = true,
    val size: DpSize = DpSize(400.dp, 300.dp),
    val title: String = "Untitled",
    val icon: Painter? = null,
    val resizable: Boolean = true,
)
