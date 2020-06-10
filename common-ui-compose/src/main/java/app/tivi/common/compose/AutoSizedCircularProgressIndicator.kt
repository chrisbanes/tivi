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
import androidx.ui.core.DensityAmbient
import androidx.ui.core.Modifier
import androidx.ui.core.WithConstraints
import androidx.ui.graphics.Color
import androidx.ui.layout.Column
import androidx.ui.layout.size
import androidx.ui.material.CircularProgressIndicator
import androidx.ui.material.MaterialTheme
import androidx.ui.material.Surface
import androidx.ui.tooling.preview.Preview
import androidx.ui.unit.dp
import androidx.ui.unit.min

@Composable
fun AutoSizedCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    WithConstraints(modifier) {
        val diameter = with(DensityAmbient.current) {
            // We need to minus the padding added within CircularProgressIndicator
            min(constraints.maxWidth.toDp(), constraints.maxHeight.toDp()) - InternalPadding
        }

        CircularProgressIndicator(
            strokeWidth = (diameter * StrokeDiameterFraction).coerceAtLeast(1.dp),
            color = color
        )
    }
}

// Default stroke size
private val DefaultStrokeWidth = 4.dp
// Preferred diameter for CircularProgressIndicator
private val DefaultDiameter = 40.dp
// Internal padding added by CircularProgressIndicator
private val InternalPadding = 4.dp

private val StrokeDiameterFraction = DefaultStrokeWidth / DefaultDiameter

@Preview
@Composable
fun previewAutoSizedCircularProgressIndicator() {
    Surface {
        Column {
            AutoSizedCircularProgressIndicator(
                modifier = Modifier.size(16.dp)
            )

            AutoSizedCircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )

            AutoSizedCircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            AutoSizedCircularProgressIndicator(
                modifier = Modifier.size(64.dp)
            )

            AutoSizedCircularProgressIndicator(
                modifier = Modifier.size(128.dp)
            )
        }
    }
}
