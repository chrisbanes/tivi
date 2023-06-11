// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TiviAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            OutlinedButton(onClick = { onConfirm() }) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(text = dismissText)
            }
        },
        onDismissRequest = onDismissRequest,
    )
}
