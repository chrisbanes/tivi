// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogButtons
import com.vanpra.composematerialdialogs.MaterialDialogProperties
import com.vanpra.composematerialdialogs.MaterialDialogScope
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.rememberMaterialDialogState

@Composable
expect fun Dialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
)

@Composable
fun TiviDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest, properties) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        ) {
            content()
        }
    }
}

@Composable
fun Material3Dialog(
    dialogState: MaterialDialogState = rememberMaterialDialogState(),
    properties: MaterialDialogProperties = MaterialDialogProperties(),
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
    shape: Shape = MaterialTheme.shapes.large,
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    autoDismiss: Boolean = true,
    onCloseRequest: (MaterialDialogState) -> Unit = { it.hide() },
    buttons: @Composable MaterialDialogButtons.() -> Unit = {},
    content: @Composable MaterialDialogScope.() -> Unit,
) {
    MaterialDialog(
        dialogState = dialogState,
        properties = properties,
        backgroundColor = backgroundColor,
        shape = shape,
        border = border,
        elevation = elevation,
        autoDismiss = autoDismiss,
        onCloseRequest = onCloseRequest,
        buttons = buttons,
        content = content,
    )
}

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
