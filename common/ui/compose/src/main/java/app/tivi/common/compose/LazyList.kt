// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

@file:Suppress("NOTHING_TO_INLINE")

package app.tivi.common.compose

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
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
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems

inline fun LazyListScope.itemSpacer(height: Dp) {
    item {
        Spacer(
            Modifier
                .height(height)
                .fillParentMaxWidth(),
        )
    }
}

inline fun LazyListScope.gutterSpacer() {
    item {
        Spacer(
            Modifier
                .height(Layout.gutter)
                .fillParentMaxWidth(),
        )
    }
}

inline fun LazyGridScope.gutterSpacer() {
    fullSpanItem {
        Spacer(
            Modifier
                .height(Layout.gutter)
                .fillMaxWidth(),
        )
    }
}

inline fun LazyGridScope.itemSpacer(height: Dp) {
    fullSpanItem {
        Spacer(
            Modifier
                .height(height)
                .fillMaxWidth(),
        )
    }
}

fun <T : Any> LazyGridScope.items(
    items: LazyPagingItems<T>,
    key: ((item: T) -> Any)? = null,
    span: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    contentType: (item: T) -> Any? = { null },
    itemContent: @Composable LazyGridItemScope.(item: T?) -> Unit,
) {
    items(
        count = items.itemCount,
        span = { index ->
            val item = items.peek(index)
            when {
                item != null && span != null -> span(item)
                else -> GridItemSpan(0)
            }
        },
        contentType = { index ->
            items.peek(index)?.let { contentType(it) }
        },
        key = if (key == null) {
            null
        } else {
            { index ->
                val item = items.peek(index)
                if (item == null) {
                    PagingPlaceholderKey(index)
                } else {
                    key(item)
                }
            }
        },
    ) { index ->
        itemContent(items[index])
    }
}

@SuppressLint("BanParcelableUsage")
internal data class PagingPlaceholderKey(private val index: Int) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) = parcel.writeInt(index)
    override fun describeContents(): Int = 0

    companion object {
        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<PagingPlaceholderKey> =
            object : Parcelable.Creator<PagingPlaceholderKey> {
                override fun createFromParcel(parcel: Parcel) =
                    PagingPlaceholderKey(parcel.readInt())

                override fun newArray(size: Int) = arrayOfNulls<PagingPlaceholderKey?>(size)
            }
    }
}

inline fun LazyGridScope.fullSpanItem(
    key: Any? = null,
    contentType: Any? = null,
    noinline content: @Composable LazyGridItemScope.() -> Unit,
) {
    item(
        key = key,
        span = { GridItemSpan(maxLineSpan) },
        contentType = contentType,
        content = content,
    )
}

/**
 * Displays a 'fake' grid using [LazyColumn]'s DSL. It's fake in that we just we add individual
 * column items, with a inner fake row.
 */
fun <T : Any> LazyListScope.itemsInGrid(
    items: List<T>,
    columns: Int,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalItemPadding: Dp = 0.dp,
    verticalItemPadding: Dp = 0.dp,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        end = contentPadding.calculateEndPadding(layoutDirection),
                    ),
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
