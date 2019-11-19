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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun <VH : RecyclerView.ViewHolder> RecyclerView.Adapter<VH>.createAndBind(parent: ViewGroup, position: Int): VH {
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
            scrollToPosition(vh.adapterPosition)
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
    targetPosition = vh.adapterPosition
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

    suspendCoroutine<Unit> { cont ->
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    cont.resume(Unit)
                }
            }
        })
    }
}