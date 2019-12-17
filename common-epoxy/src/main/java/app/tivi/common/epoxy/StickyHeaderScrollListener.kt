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

package app.tivi.common.epoxy

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import app.tivi.extensions.createAndBind
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel

/**
 * Taken from https://gist.github.com/jonasbark/f1e1373705cfe8f6a7036763f7326f7c
 */
class StickyHeaderScrollListener(
    private val epoxyController: EpoxyController,
    private val isHeader: (EpoxyModel<*>) -> Boolean,
    private val headerHolder: ViewGroup
) : RecyclerView.OnScrollListener() {

    init {
        syncToHeaderHolder(null)
    }

    private var currentHeaderItemPosition = RecyclerView.NO_POSITION
        set(position) {
            if (field != position) {
                currentHeaderHolder = null
                if (position != RecyclerView.NO_POSITION) {
                    currentHeaderHolder = epoxyController.adapter.createAndBind(headerHolder, position)
                }
                field = position
            }
        }

    private var currentHeaderHolder: RecyclerView.ViewHolder? = null
        set(value) {
            if (field != value) {
                syncToHeaderHolder(value)
                field = value
            }
        }

    private val currentHeaderHeight: Int
        get() = headerHolder.height

    override fun onScrolled(parent: RecyclerView, dx: Int, dy: Int) {
        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPositionForChild = getHeaderPositionForItem(topChildPosition)
        currentHeaderItemPosition = when {
            headerPositionForChild == 0 && topChildPosition == 0 && topChild.top >= 0 -> RecyclerView.NO_POSITION
            else -> headerPositionForChild
        }

        if (currentHeaderItemPosition != RecyclerView.NO_POSITION) {
            val childInContact = getChildInContact(parent, currentHeaderHeight, currentHeaderItemPosition)
            if (childInContact != null) {
                val childContactPosition = parent.getChildAdapterPosition(childInContact)
                if (isHeader(childContactPosition)) {
                    headerHolder.translationY = (childInContact.top - currentHeaderHeight).toFloat()
                } else {
                    headerHolder.translationY = 0f
                }
            }
        }
    }

    private fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition != RecyclerView.NO_POSITION) {
            val model = epoxyController.adapter.getModelAtPosition(itemPosition)
            return isHeader(model)
        }
        return false
    }

    private fun getChildInContact(
        parent: RecyclerView,
        contactPoint: Int,
        currentHeaderPos: Int
    ): View? {
        for (i in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(i)

            // measure height tolerance with child if child is another header
            if (currentHeaderPos != i && isHeader(parent.getChildAdapterPosition(child))) {
                heightTolerance = currentHeaderHeight - child.height
            }

            // add heightTolerance if child top be in display area
            val childBottomPosition = if (child.top > 0) child.bottom + heightTolerance else child.bottom

            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    return child
                }
                break
            }
        }
        return null
    }

    /**
     * This method gets called by [StickyHeaderScrollListener] to fetch the position of the header item in the adapter
     * that is used for (represents) item at specified position.
     * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
     * @return int. Position of the header item in the adapter.
     */
    private fun getHeaderPositionForItem(itemPosition: Int): Int {
        return (itemPosition downTo 0).firstOrNull(::isHeader) ?: RecyclerView.NO_POSITION
    }

    private fun syncToHeaderHolder(header: RecyclerView.ViewHolder?) {
        if (header != null) {
            if (headerHolder.indexOfChild(header.itemView) < 0) {
                headerHolder.addView(header.itemView)
            }
            headerHolder.isVisible = true
        } else {
            if (headerHolder.childCount > 0) {
                headerHolder.removeAllViews()
            }
            headerHolder.isVisible = false
        }
    }
}
