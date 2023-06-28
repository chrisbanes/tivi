// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.runtime.Composable
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.message
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
    MaterialDialog(
        onCloseRequest = { onDismissRequest() },
    ) {
        title(title)
        message(message)
        dialogButtons.positiveButton(text = confirmText, onClick = onConfirm)
        dialogButtons.negativeButton(text = dismissText, onClick = onDismissRequest)
    }
}
