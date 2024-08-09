// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import app.tivi.common.ui.resources.Res
import app.tivi.common.ui.resources.cd_clear_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun SearchTextField(
  value: TextFieldValue,
  onValueChange: (TextFieldValue) -> Unit,
  hint: String,
  modifier: Modifier = Modifier,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions(),
  onCleared: (() -> Unit) = { onValueChange(TextFieldValue()) },
) {
  val keyboardController = LocalSoftwareKeyboardController.current

  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    leadingIcon = {
      Icon(
        imageVector = Icons.Default.Search,
        contentDescription = null, // decorative
      )
    },
    trailingIcon = {
      IconButton(
        onClick = {
          onCleared()
          // This is mostly for iOS, otherwise there is no way to dismiss the iOS
          // keyboard once opened.
          keyboardController?.hide()
        },
      ) {
        Icon(
          imageVector = Icons.Default.Clear,
          contentDescription = stringResource(Res.string.cd_clear_text),
        )
      }
    },
    placeholder = { Text(text = hint) },
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    maxLines = 1,
    singleLine = true,
    modifier = modifier,
  )
}
