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

import androidx.compose.Composable
import androidx.compose.MutableState
import androidx.compose.state
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.Popup
import androidx.ui.core.PopupProperties
import androidx.ui.foundation.Clickable
import androidx.ui.foundation.Text
import androidx.ui.layout.Column
import androidx.ui.layout.padding
import androidx.ui.layout.preferredSizeIn
import androidx.ui.layout.wrapContentHeight
import androidx.ui.layout.wrapContentWidth
import androidx.ui.material.EmphasisAmbient
import androidx.ui.material.MaterialTheme
import androidx.ui.material.ProvideEmphasis
import androidx.ui.material.Surface
import androidx.ui.material.ripple.ripple
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxPosition
import androidx.ui.unit.dp

@Composable
fun PopupMenu(
    items: List<PopupMenuItem>,
    visible: MutableState<Boolean> = state { true },
    alignment: Alignment = Alignment.TopStart,
    offset: IntPxPosition = IntPxPosition(IntPx.Zero, IntPx.Zero)
) {
    if (visible.value) {
        Popup(
            alignment = alignment,
            offset = offset,
            popupProperties = PopupProperties(
                isFocusable = true,
                onDismissRequest = { visible.value = false }
            )
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
                        Clickable(
                            onClick = {
                                item.onClick?.invoke()
                                visible.value = false
                            },
                            enabled = item.enabled,
                            modifier = Modifier.ripple(enabled = item.enabled)
                        ) {
                            val emphasis = when {
                                item.enabled -> EmphasisAmbient.current.high
                                else -> EmphasisAmbient.current.disabled
                            }
                            ProvideEmphasis(emphasis) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.body1,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
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
