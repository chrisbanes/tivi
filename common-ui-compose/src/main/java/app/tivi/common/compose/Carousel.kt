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

import androidx.compose.foundation.layout.InnerPadding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRowFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun <T> Carousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    contentPadding: InnerPadding = InnerPadding(0.dp),
    itemSpacing: Dp = 0.dp,
    verticalGravity: Alignment.Vertical = Alignment.Top,
    itemContent: @Composable LazyItemScope.(T, InnerPadding) -> Unit
) {
    val halfSpacing = itemSpacing / 2
    val spacingContent = InnerPadding(halfSpacing, 0.dp, halfSpacing, 0.dp)

    LazyRowFor(
        items = items,
        modifier = modifier,
        contentPadding = contentPadding.copy(
            start = (contentPadding.start - halfSpacing).coerceAtLeast(0.dp),
            end = (contentPadding.end - halfSpacing).coerceAtLeast(0.dp)
        ),
        verticalGravity = verticalGravity,
        itemContent = { item -> itemContent(item, spacingContent) }
    )
}
