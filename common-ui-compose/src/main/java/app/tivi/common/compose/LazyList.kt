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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.foundation.layout.preferredWidth
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
import app.tivi.common.compose.paging.LazyPagingItems

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

/**
 * Displays a 'fake' grid using [LazyColumn]'s DSL. It's fake in that we just we add individual
 * column items, with a inner fake row.
 */
fun <T : Any> LazyListScope.fakeGridItems(
    lazyPagingItems: LazyPagingItems<T>,
    columns: Int,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalItemPadding: Dp = 0.dp,
    verticalItemPadding: Dp = 0.dp,
    itemContent: @Composable (T?) -> Unit
) {
    lazyPagingItems.loadState

    val rows = when {
        lazyPagingItems.itemCount % columns == 0 -> lazyPagingItems.itemCount / columns
        else -> (lazyPagingItems.itemCount / columns) + 1
    }

    for (row in 0 until rows) {
        if (row == 0) spacerItem(contentPadding.top)

        item {
            Row(
                Modifier.fillMaxWidth()
                    .padding(start = contentPadding.start, end = contentPadding.end)
            ) {
                for (column in 0 until columns) {
                    Box(modifier = Modifier.weight(1f)) {
                        val index = (row * columns) + column
                        if (index < lazyPagingItems.itemCount) {
                            itemContent(lazyPagingItems[index])
                        }
                    }
                    if (column < columns - 1) {
                        Spacer(modifier = Modifier.preferredWidth(horizontalItemPadding))
                    }
                }
            }
        }

        if (row < rows - 1) {
            spacerItem(verticalItemPadding)
        } else {
            spacerItem(contentPadding.bottom)
        }
    }
}

/**
 * Displays a 'fake' grid using [LazyColumn]'s DSL. It's fake in that we just we add individual
 * column items, with a inner fake row.
 */
fun <T> LazyListScope.fakeGridItems(
    items: List<T>,
    columns: Int,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalItemPadding: Dp = 0.dp,
    verticalItemPadding: Dp = 0.dp,
    itemContent: @Composable (T) -> Unit
) {
    val rows = when {
        items.size % columns == 0 -> items.size / columns
        else -> (items.size / columns) + 1
    }

    for (row in 0 until rows) {
        if (row == 0) spacerItem(contentPadding.top)

        item {
            Row(
                Modifier.fillMaxWidth()
                    .padding(start = contentPadding.start, end = contentPadding.end)
            ) {
                for (column in 0 until columns) {
                    Box(modifier = Modifier.weight(1f)) {
                        val index = (row * columns) + column
                        if (index < items.size) {
                            itemContent(items[index])
                        }
                    }
                    if (column < columns - 1) {
                        Spacer(modifier = Modifier.preferredWidth(horizontalItemPadding))
                    }
                }
            }
        }

        if (row < rows - 1) {
            spacerItem(verticalItemPadding)
        } else {
            spacerItem(contentPadding.bottom)
        }
    }
}
