/*
 * Copyright 2019 Google LLC
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

package app.tivi.extensions

import android.view.ViewGroup
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import app.tivi.ui.recyclerview.TiviLinearSmoothScroller
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.createAndBind(
    parent: ViewGroup,
    position: Int
): VH {
    val vh = onCreateViewHolder(parent, getItemViewType(position))
    onBindViewHolder(vh, position)
    return vh
}

fun RecyclerView.scrollToItemId(itemId: Long, animatedScroll: Boolean = false): Boolean {
    val vh = findViewHolderForItemId(itemId)
    if (vh != null) {
        if (animatedScroll) {
            smoothScrollToViewHolder(vh)
        } else {
            scrollToPosition(vh.bindingAdapterPosition)
        }
        return true
    }
    return false
}

fun RecyclerView.smoothScrollToViewHolder(vh: RecyclerView.ViewHolder) = TiviLinearSmoothScroller(
    context,
    snapPreference = LinearSmoothScroller.SNAP_TO_START,
    scrollMsPerInch = 60f
).apply {
    targetPosition = vh.bindingAdapterPosition
    targetOffset = vh.itemView.height / 3
}.also {
    layoutManager!!.startSmoothScroll(it)
}

fun RecyclerView.smoothScrollToItemPosition(position: Int) = TiviLinearSmoothScroller(
    context,
    snapPreference = LinearSmoothScroller.SNAP_TO_START,
    scrollMsPerInch = 60f
).apply {
    targetPosition = position
}.also {
    layoutManager!!.startSmoothScroll(it)
}

suspend fun RecyclerView.awaitScrollEnd() {
    // If a smooth scroll has just been started, it won't actually start until the next
    // animation frame, so we'll await that first
    awaitAnimationFrame()
    // Now we can check if we're actually idle. If so, return now
    if (scrollState == RecyclerView.SCROLL_STATE_IDLE) return

    suspendCancellableCoroutine<Unit> { cont ->
        cont.invokeOnCancellation {
            // If the coroutine is cancelled, stop the RecyclerView scrolling
            stopScroll()
        }

        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Make sure we remove the listener so we don't keep leak the
                    // coroutine continuation
                    recyclerView.removeOnScrollListener(this)
                    // Finally, resume the coroutine
                    cont.resume(Unit)
                }
            }
        })
    }
}

/**
 * Finds the first item with the given [itemId] in the data set, or [RecyclerView.NO_POSITION] if
 * there is no item with the id.
 */
fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.findItemIdPosition(itemId: Long): Int {
    return (0 until itemCount).firstOrNull { getItemId(it) == itemId } ?: RecyclerView.NO_POSITION
}

/**
 * Await an item in the data set with the given [itemId], and return its adapter position.
 */
suspend fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.awaitItemIdExists(itemId: Long): Int {
    val currentPos = findItemIdPosition(itemId)
    // If the item is already in the data set, return the position now
    if (currentPos >= 0) return currentPos

    // Otherwise we register a data set observer and wait for the item ID to be added
    return suspendCancellableCoroutine { cont ->
        val observer = object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                (positionStart until positionStart + itemCount).forEach { position ->
                    // Iterate through the new items and check if any have our itemId
                    if (getItemId(position) == itemId) {
                        // Remove this observer so we don't leak the coroutine
                        unregisterAdapterDataObserver(this)
                        // And resume the coroutine
                        cont.resume(position)
                    }
                }
            }
        }
        // If the coroutine is cancelled, remove the observer
        cont.invokeOnCancellation { unregisterAdapterDataObserver(observer) }
        // And finally register the observer
        registerAdapterDataObserver(observer)
    }
}
