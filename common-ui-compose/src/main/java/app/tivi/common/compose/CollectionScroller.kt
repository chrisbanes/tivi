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
import androidx.ui.core.Modifier
import androidx.ui.foundation.HorizontalScroller
import androidx.ui.layout.RowScope
import androidx.ui.layout.Spacer
import androidx.ui.layout.preferredWidth
import androidx.ui.unit.Dp
import androidx.ui.unit.dp

@Composable
fun <T> HorizontalCollectionScroller(
    items: List<T>,
    startEndSpacing: Dp = 16.dp,
    childSpacing: Dp = 4.dp,
    modifier: Modifier = Modifier,
    children: @Composable RowScope.(item: T) -> Unit
) {
    HorizontalScroller(modifier = modifier) {
        items.forEachIndexed { index, item ->
            if (index == 0) {
                // First item, so lets add the starting margin
                Spacer(modifier = Modifier.preferredWidth(startEndSpacing))
            }

            children(item)

            if (index < items.lastIndex) {
                // Add a spacer if there are still more items to add
                Spacer(modifier = Modifier.preferredWidth(childSpacing))
            } else {
                // Last item, so lets add the end margin
                Spacer(modifier = Modifier.preferredWidth(startEndSpacing))
            }
        }
    }
}
