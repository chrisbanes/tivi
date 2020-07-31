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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.EmphasisAmbient
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup

@Composable
fun PopupMenu(
    items: List<PopupMenuItem>,
    onDismiss: () -> Unit,
    alignment: Alignment = Alignment.TopStart,
    offset: IntOffset = IntOffset(0, 0)
) {
    Popup(
        alignment = alignment,
        offset = offset,
        isFocusable = true,
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            elevation = 2.dp
        ) {
            Column(
                Modifier.wrapContentWidth(align = Alignment.Start)
                    .wrapContentHeight(align = Alignment.Top)
                    .preferredSizeIn(minWidth = 96.dp, maxWidth = 192.dp)
            ) {
                items.forEach { item ->
                    val emphasis = when {
                        item.enabled -> EmphasisAmbient.current.high
                        else -> EmphasisAmbient.current.disabled
                    }
                    ProvideEmphasis(emphasis) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.body1,
                            modifier = Modifier.clickable(
                                onClick = {
                                    item.onClick?.invoke()
                                    onDismiss()
                                },
                                enabled = item.enabled
                            )
                                .padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

data class PopupMenuItem(
    val title: String,
    val enabled: Boolean = true,
    val onClick: (() -> Unit)? = null
)
