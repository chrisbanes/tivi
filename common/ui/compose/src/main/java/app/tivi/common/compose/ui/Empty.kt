/*
 * Copyright 2023 Google LLC
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

package app.tivi.common.compose.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EmptyContent(
    graphic: @Composable () -> Unit,
    title: @Composable () -> Unit,
    prompt: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(modifier = Modifier.align(Alignment.Center)) {
            val density = LocalDensity.current
            val emojiHeaderGraphicTextStyle = remember(density) {
                TextStyle(
                    // We don't want font scaling to affect this size
                    fontSize = 112.dp.asEm(density),
                    // Any opaque color will work here
                    color = Color.Magenta,
                )
            }

            ProvideTextStyle(emojiHeaderGraphicTextStyle) {
                Box(Modifier.align(Alignment.CenterHorizontally)) {
                    graphic()
                }
            }
            ProvideTextStyle(MaterialTheme.typography.headlineLarge) {
                title()
            }
            ProvideTextStyle(MaterialTheme.typography.bodyLarge) {
                prompt()
            }
        }
    }
}

private fun Dp.asEm(density: Density): TextUnit = (value / density.fontScale).sp
