/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.common.compose

import androidx.compose.foundation.Text
import androidx.compose.material.AlertDialog
import androidx.compose.material.OutlinedButton
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable

@Composable
fun TiviAlertDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismissRequest: () -> Unit
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
        onDismissRequest = onDismissRequest
    )
}
