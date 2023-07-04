// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.unit.dp
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.message
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title

@Composable
fun TiviAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismissRequest: () -> Unit,
) {
    val dialogState = rememberMaterialDialogState()

    LaunchedEffect(dialogState) {
        dialogState.show()
    }

    val lastOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val onDismiss = {
        dialogState.hide()
        lastOnDismissRequest()
    }

    MaterialDialog(
        dialogState = dialogState,
        backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp),
        shape = MaterialTheme.shapes.medium,
        elevation = 0.dp,
        onCloseRequest = { onDismiss() },
        buttons = {
            negativeButton(
                text = dismissText,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = onDismiss,
            )

            positiveButton(
                text = confirmText,
                textStyle = MaterialTheme.typography.labelLarge,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = onConfirm,
            )
        },
    ) {
        title(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
        )

        message(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
