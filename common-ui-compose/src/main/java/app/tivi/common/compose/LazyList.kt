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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.lazy.ExperimentalLazyDsl
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun LazyListScope.spacerItem(height: Dp) {
    item {
        Spacer(Modifier.preferredHeight(height).fillParentMaxWidth())
    }
}

/**
 * Provides a workaround for https://issuetracker.google.com/167913500
 */
@OptIn(ExperimentalLazyDsl::class)
@Composable
fun <T> WorkaroundLazyColumnFor(
    items: List<T>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding.copy(top = 0.dp, bottom = 0.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        if (contentPadding.top > 0.dp) {
            item { Spacer(Modifier.preferredHeight(contentPadding.top)) }
        }

        items(items, itemContent)

        if (contentPadding.bottom > 0.dp) {
            item { Spacer(Modifier.preferredHeight(contentPadding.bottom)) }
        }
    }
}
