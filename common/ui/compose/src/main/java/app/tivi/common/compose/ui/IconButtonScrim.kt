/*
 * Copyright 2021 Google LLC
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

import androidx.annotation.FloatRange
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.theme.TiviTheme

@Composable
fun ScrimmedIconButton(
    showScrim: Boolean,
    onClick: () -> Unit,
    invertThemeOnScrim: Boolean = true,
    icon: @Composable () -> Unit,
) {
    IconButton(onClick = onClick) {
        if (invertThemeOnScrim) {
            val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5

            Crossfade(targetState = showScrim) { show ->
                TiviTheme(useDarkColors = if (show) isLight else !isLight) {
                    ScrimSurface(showScrim = show, icon = icon)
                }
            }
        } else {
            ScrimSurface(showScrim = showScrim, icon = icon)
        }
    }
}

@Composable
private fun ScrimSurface(
    modifier: Modifier = Modifier,
    showScrim: Boolean = true,
    @FloatRange(from = 0.0, to = 1.0) alpha: Float = 0.3f,
    icon: @Composable () -> Unit,
) {
    Surface(
        color = when {
            showScrim -> MaterialTheme.colorScheme.surface.copy(alpha = alpha)
            else -> Color.Transparent
        },
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
        modifier = modifier,
        content = {
            Box(Modifier.padding(4.dp)) {
                icon()
            }
        },
    )
}
