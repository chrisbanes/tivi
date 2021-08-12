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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.copy(
    copyStart: Boolean = true,
    copyTop: Boolean = true,
    copyEnd: Boolean = true,
    copyBottom: Boolean = true,
): PaddingValues {
    return remember(this) {
        derivedStateOf {
            PaddingValues(
                start = if (copyStart) calculateStartPadding(LayoutDirection.Ltr) else 0.dp,
                top = if (copyTop) calculateTopPadding() else 0.dp,
                end = if (copyEnd) calculateEndPadding(LayoutDirection.Ltr) else 0.dp,
                bottom = if (copyBottom) calculateBottomPadding() else 0.dp,
            )
        }
    }.value
}
