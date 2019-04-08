/*
 * Copyright 2019 Google, Inc.
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

package app.tivi.ui.epoxy

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.withTranslation
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView

/**
 * Taken from https://gist.github.com/jonasbark/f1e1373705cfe8f6a7036763f7326f7c
 */
class StickyHeaderItemDecoration(
    private val epoxyController: EpoxyController,
    private val isHeader: (EpoxyModel<*>) -> Boolean,
    private val headerBackground: Drawable? = null
) : RecyclerView.ItemDecoration() {
    private var stickyHeaderHeight: Int = 0

    private var currentHeaderItemPosition = RecyclerView.NO_POSITION
    private var currentHeader: RecyclerView.ViewHolder? = null

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent !is EpoxyRecyclerView) {
            throw IllegalArgumentException("This Item Decoration must be used with EpoxyRecyclerView")
        }

        val topChild = parent.getChildAt(0) ?: return

        val topChildPosition = parent.getChildAdapterPosition(topChild)
        if (topChildPosition == RecyclerView.NO_POSITION) {
            return
        }

        val headerPos = getHeaderPositionForItem(topChildPosition)
        if (headerPos != RecyclerView.NO_POSITION) {
            if (headerPos != currentHeaderItemPosition) {
                currentHeader = createViewHolderForPosition(headerPos, parent).also {
                    layoutViewHolder(parent, it.itemView)
                }
                currentHeaderItemPosition = headerPos
            }

            val header = currentHeader
            if (header != null) {
                val childInContact = getChildInContact(parent, header.itemView.height, headerPos)

                var offset = 0
                if (childInContact != null && isHeader(parent.getChildAdapterPosition(childInContact))) {
                    offset = childInContact.top - header.itemView.height
                }

                headerBackground?.setBounds(0, 0, header.itemView.width, header.itemView.height)

                c.withTranslation(parent.paddingLeft.toFloat(), offset.toFloat()) {
                    headerBackground?.draw(this)
                    header.itemView.draw(this)
                }
            }
        } else {
            currentHeader = null
            currentHeaderItemPosition = RecyclerView.NO_POSITION
        }
    }

    private fun createViewHolderForPosition(position: Int, parent: EpoxyRecyclerView): RecyclerView.ViewHolder {
        val vh = epoxyController.adapter.onCreateViewHolder(parent, epoxyController.adapter.getItemViewType(position))
        epoxyController.adapter.onBindViewHolder(vh, position)
        return vh
    }

    private fun isHeader(itemPosition: Int): Boolean {
        if (itemPosition != RecyclerView.NO_POSITION) {
            val model = epoxyController.adapter.getModelAtPosition(itemPosition)
            return isHeader(model)
        }
        return false
    }

    private fun getChildInContact(parent: RecyclerView, contactPoint: Int, currentHeaderPos: Int): View? {
        var childInContact: View? = null
        for (i in 0 until parent.childCount) {
            var heightTolerance = 0
            val child = parent.getChildAt(i)

            //measure height tolerance with child if child is another header
            if (currentHeaderPos != i) {
                val isChildHeader = isHeader(parent.getChildAdapterPosition(child))
                if (isChildHeader) {
                    heightTolerance = stickyHeaderHeight - child.height
                }
            }

            //add heightTolerance if child top be in display area
            val childBottomPosition = if (child.top > 0) {
                child.bottom + heightTolerance
            } else {
                child.bottom
            }

            if (childBottomPosition > contactPoint) {
                if (child.top <= contactPoint) {
                    // This child overlaps the contactPoint
                    childInContact = child
                    break
                }
            }
        }
        return childInContact
    }

    /**
     * This method gets called by [StickyHeaderItemDecoration] to fetch the position of the header item in the adapter
     * that is used for (represents) item at specified position.
     * @param itemPosition int. Adapter's position of the item for which to do the search of the position of the header item.
     * @return int. Position of the header item in the adapter.
     */
    private fun getHeaderPositionForItem(itemPosition: Int): Int {
        var tempPosition = itemPosition
        var headerPosition = RecyclerView.NO_POSITION
        do {
            if (isHeader(tempPosition)) {
                headerPosition = tempPosition
                break
            }
            tempPosition -= 1
        } while (tempPosition >= -1)
        return headerPosition
    }

    /**
     * Properly measures and layouts the top sticky header.
     * @param parent ViewGroup: RecyclerView in this case.
     */
    private fun layoutViewHolder(parent: ViewGroup, view: View) {
        // Specs for parent (RecyclerView)
        val widthSpec = View.MeasureSpec.makeMeasureSpec(parent.width, View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(parent.height, View.MeasureSpec.UNSPECIFIED)

        // Specs for children (headers)
        val childWidthSpec = ViewGroup.getChildMeasureSpec(
                widthSpec,
                parent.paddingLeft + parent.paddingRight,
                view.layoutParams.width
        )
        val childHeightSpec = ViewGroup.getChildMeasureSpec(
                heightSpec,
                parent.paddingTop + parent.paddingBottom,
                view.layoutParams.height
        )

        view.measure(childWidthSpec, childHeightSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }
}