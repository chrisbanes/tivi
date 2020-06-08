/*
 * Copyright 2017 Google LLC
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

package app.tivi.ui.transitions

import android.animation.Animator
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionValues
import app.tivi.extensions.animatorSetOf

/**
 * This transition captures the layout bounds of target views before and after
 * the scene change and animates those changes during the transition.
 *
 * A ChangeBounds transition can be described in a resource file by using the
 * tag `changeBounds`, along with the other standard attributes of Transition.
 */
class ColumnedChangeBounds : ViewChangeBounds() {

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val view = endValues.view

        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect

        val startLeft = startBounds.left
        val endLeft = endBounds.left

        val startTop = startBounds.top
        val startBottom = startBounds.bottom

        val startWidth = startBounds.width()
        val startHeight = startBounds.height()

        val endWidth = endBounds.width()

        // If we don't have a RecyclerView parent, not much point us doing anything so return null
        val recyclerViewEndParent = findRecyclerViewParent(view) ?: return null

        val anim: Animator

        if (endWidth > startWidth && endLeft < startLeft) {
            // If we're bigger, and we're going left, we're probably part of a grid of different column
            // counts
            val anims = mutableListOf<Animator>()

            // Animate the current view in from the left
            val startCopy = Rect(endLeft - startWidth, startBottom, endLeft, startBottom + startHeight)
            anims += createPointToPointAnimator(view, startCopy, endBounds, 0f, 1f)

            // Animate a copy of the current view out to the right
            createAnimatorsForChildCopyOutRight(view, recyclerViewEndParent, startBounds, endBounds)?.also {
                anims += it
            }

            anim = animatorSetOf(anims)
        } else if (endWidth < startWidth && endLeft > startLeft) {
            // If we're smaller, and we're going right, we're probably part of a grid of different column
            // counts
            val anims = mutableListOf<Animator>()

            val gap = 0 // TODO Fix this spacing
            val inStartLeft = startLeft + sceneRoot.width
            val inStartTop = startTop - startHeight - gap
            val startCopy = Rect(inStartLeft, inStartTop, inStartLeft + startWidth, inStartTop + startHeight)

            anims += createPointToPointAnimator(view, startCopy, endBounds, 0f, 1f)

            // Animate a copy of the current view out to the left
            anims += createAnimatorForChildCopyOutLeft(view, startBounds, endBounds)

            anim = animatorSetOf(anims)
        } else {
            anim = createPointToPointAnimator(view, startBounds, endBounds)
        }

        val parent = view.parent
        if (parent is ViewGroup) {
            parent.suppressLayoutInternal(true)

            addListener(object : TransitionListenerAdapter() {
                private var canceled = false

                override fun onTransitionCancel(transition: Transition) {
                    parent.suppressLayoutInternal(false)
                    canceled = true
                }

                override fun onTransitionEnd(transition: Transition) {
                    if (!canceled) {
                        parent.suppressLayoutInternal(false)
                    }
                    transition.removeListener(this)
                }

                override fun onTransitionPause(transition: Transition) {
                    parent.suppressLayoutInternal(false)
                }

                override fun onTransitionResume(transition: Transition) {
                    parent.suppressLayoutInternal(false)
                }
            })
        }

        return anim
    }

    private fun createAnimatorForChildCopyOutLeft(
        view: View,
        startBounds: Rect,
        endBounds: Rect
    ): Animator {
        val gap = 0 // TODO
        val newEndRight = startBounds.left - gap
        val newEndTop = endBounds.bottom + gap
        val newEndBounds = Rect(
            newEndRight - endBounds.width(),
            newEndTop,
            newEndRight,
            newEndTop + endBounds.height()
        )

        return createPointToPointAnimator(view, startBounds, newEndBounds, 1f, 0f, true)
    }

    private fun createAnimatorsForChildCopyOutRight(
        view: View,
        endParent: RecyclerView,
        startBounds: Rect,
        endBounds: Rect
    ): Animator? {
        val prevEndBounds = findPreviousItemViewWithGreaterRight(endParent, view, endBounds.right)
            ?: return null

        val newEndLeft = prevEndBounds.right + endBounds.left
        val newEndTop = prevEndBounds.top
        val newEndBounds = Rect(
            newEndLeft, newEndTop,
            newEndLeft + endBounds.width(),
            newEndTop + endBounds.height()
        )

        return createPointToPointAnimator(view, startBounds, newEndBounds, 1f, 0f, true)
    }

    private fun findRecyclerViewParent(view: View): RecyclerView? {
        var parent = view.parent
        while (parent != null && parent !is RecyclerView) {
            parent = parent.parent
        }
        return when (parent) {
            is RecyclerView -> parent
            else -> null
        }
    }

    private fun findPreviousItemViewWithGreaterRight(
        parent: RecyclerView,
        view: View,
        right: Int
    ): Rect? {
        // This view might not necessarily be the root of the item view
        val rvItemView = parent.findContainingItemView(view) ?: return null

        for (i in parent.getChildAdapterPosition(rvItemView) - 1 downTo 0) {
            val vh = parent.findViewHolderForAdapterPosition(i) ?: continue
            val tv = getTransitionValues(vh.itemView, false) ?: continue
            val bounds = tv.values[PROPNAME_BOUNDS] as? Rect ?: continue
            if (bounds.right > right) {
                return bounds
            }
        }

        return null
    }
}
