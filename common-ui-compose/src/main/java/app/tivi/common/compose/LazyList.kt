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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.tivi.common.compose.paging.LazyPagingItems

fun LazyListScope.itemSpacer(height: Dp) {
    item {
        Spacer(Modifier.height(height).fillParentMaxWidth())
    }
}

/**
 * Displays a 'fake' grid using [LazyColumn]'s DSL. It's fake in that we just we add individual
 * column items, with a inner fake row.
 */
fun <T : Any> LazyListScope.itemsInGrid(
    lazyPagingItems: LazyPagingItems<T>,
    columns: Int,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalItemPadding: Dp = 0.dp,
    verticalItemPadding: Dp = 0.dp,
    itemContent: @Composable (T?) -> Unit
) {
    val rows = when {
        lazyPagingItems.itemCount % columns == 0 -> lazyPagingItems.itemCount / columns
        else -> (lazyPagingItems.itemCount / columns) + 1
    }

    for (row in 0 until rows) {
        if (row == 0) itemSpacer(contentPadding.calculateTopPadding())

        item {
            val layoutDirection = LocalLayoutDirection.current
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection)
                    )
            ) {
                for (column in 0 until columns) {
                    Box(modifier = Modifier.weight(1f)) {
                        val index = (row * columns) + column
                        if (index < lazyPagingItems.itemCount) {
                            itemContent(lazyPagingItems[index])
                        }
                    }
                    if (column < columns - 1) {
                        Spacer(modifier = Modifier.width(horizontalItemPadding))
                    }
                }
            }
        }

        if (row < rows - 1) {
            itemSpacer(verticalItemPadding)
        } else {
            itemSpacer(contentPadding.calculateBottomPadding())
        }
    }
}

/**
 * Displays a 'fake' grid using [LazyColumn]'s DSL. It's fake in that we just we add individual
 * column items, with a inner fake row.
 */
fun <T> LazyListScope.itemsInGrid(
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
        if (row == 0) itemSpacer(contentPadding.calculateTopPadding())

        item {
            val layoutDirection = LocalLayoutDirection.current

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection)
                    )
            ) {
                for (column in 0 until columns) {
                    Box(modifier = Modifier.weight(1f)) {
                        val index = (row * columns) + column
                        if (index < items.size) {
                            itemContent(items[index])
                        }
                    }
                    if (column < columns - 1) {
                        Spacer(modifier = Modifier.width(horizontalItemPadding))
                    }
                }
            }
        }

        if (row < rows - 1) {
            itemSpacer(verticalItemPadding)
        } else {
            itemSpacer(contentPadding.calculateBottomPadding())
        }
    }
}
