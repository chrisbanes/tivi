/*
 * Copyright 2017 Google, Inc.
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

package android.support.transition

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.view.ViewCompat
import android.support.v7.widget.RecyclerView
import android.util.Property
import android.view.View
import android.view.ViewGroup
import me.banes.chris.tivi.ui.transitions.DrawableAlphaProperty

/**
 * This transition captures the layout bounds of target views before and after
 * the scene change and animates those changes during the transition.
 *
 * A ChangeBounds transition can be described in a resource file by using the
 * tag `changeBounds`, along with the other standard attributes of Transition.
 */
class ColumnedChangeBounds : Transition() {

    override fun getTransitionProperties(): Array<String>? = TRANSITION_PROPS

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        if (ViewCompat.isLaidOut(view) || view.width != 0 || view.height != 0) {
            val loc = TEMP_ARRAY
            view.getLocationOnScreen(loc)

            values.values.put(PROPNAME_BOUNDS, Rect(loc[0], loc[1],
                    loc[0] + view.width, loc[1] + view.height))
            values.values.put(PROPNAME_PARENT, values.view.parent)
        }
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues)
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val startView = startValues.view
        val endView = endValues.view

        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect

        val startLeft = startBounds.left
        val endLeft = endBounds.left

        val startTop = startBounds.top
        val endTop = endBounds.top

        val startRight = startBounds.right
        val endRight = endBounds.right

        val startBottom = startBounds.bottom
        val endBottom = endBounds.bottom

        val startWidth = startRight - startLeft
        val startHeight = startBottom - startTop

        val endWidth = endRight - endLeft

        // If we don't have a RecyclerView parent, not much point us doing anything so return null
        val recyclerViewEndParent = findRecyclerViewParent(endView) ?: return null

        val anim: Animator

        if (endWidth > startWidth && endLeft < startLeft) {
            // If we're bigger, and we're going left, we're probably part of a grid of different column
            // counts
            val anims = mutableListOf<Animator>()

            // Animate the current view in from the left
            val startCopy = Rect(endLeft - startWidth, startBottom, endLeft, startBottom + startHeight)
            anims += createPointToPointAnimator(sceneRoot, startView, startCopy, endBounds, 0)

            // Animate a copy of the current view out to the right
            createAnimatorsForChildCopyOutRight(sceneRoot, startView, recyclerViewEndParent, startBounds, endBounds)?.let {
                anims += it
            }

            anim = AnimatorSet().apply { playTogether(anims) }
        } else if (endWidth < startWidth && endLeft > startLeft) {
            // If we're smaller, and we're going right, we're probably part of a grid of different column
            // counts
            val anims = mutableListOf<Animator>()

            val gap = 0 // TODO Fix this spacing
            val inStartLeft = startLeft + sceneRoot.width
            val inStartTop = startTop - startHeight - gap
            val startCopy = Rect(inStartLeft, inStartTop, inStartLeft + startWidth, inStartTop + startHeight)

            anims += createPointToPointAnimator(sceneRoot, startView, startCopy, endBounds, 0)

            // Animate a copy of the current view out to the left
            anims += createAnimatorForChildCopyOutLeft(sceneRoot, startView, startBounds, endBounds)

            anim = AnimatorSet().apply { playTogether(anims) }
        } else {
            anim = createPointToPointAnimator(sceneRoot, startView, startBounds, endBounds)
        }

        endView.visibility = View.INVISIBLE
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                endView.visibility = View.VISIBLE
            }
        })

        if (startView.parent is ViewGroup) {
            val parent = startView.parent as ViewGroup
            ViewGroupUtils.suppressLayout(parent, true)
            addListener(object : TransitionListenerAdapter() {
                private var canceled = false

                override fun onTransitionCancel(transition: Transition) {
                    ViewGroupUtils.suppressLayout(parent, false)
                    canceled = true
                }

                override fun onTransitionEnd(transition: Transition) {
                    if (!canceled) {
                        ViewGroupUtils.suppressLayout(parent, false)
                    }
                    transition.removeListener(this)
                }

                override fun onTransitionPause(transition: Transition) {
                    ViewGroupUtils.suppressLayout(parent, false)
                }

                override fun onTransitionResume(transition: Transition) {
                    ViewGroupUtils.suppressLayout(parent, true)
                }
            })
        }

        return anim
    }

    private fun createAnimatorForChildCopyOutLeft(
        sceneRoot: ViewGroup,
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
                newEndTop + endBounds.height())

        return createPointToPointAnimator(sceneRoot, view, startBounds, newEndBounds, 255, 0)
    }

    private fun createAnimatorsForChildCopyOutRight(
        sceneRoot: ViewGroup,
        view: View,
        endParent: RecyclerView,
        startBounds: Rect,
        endBounds: Rect
    ): Animator? {
        val prevEndBounds = findPreviousItemViewWithGreaterRight(endParent, view, endBounds.right) ?: return null

        val newEndLeft = prevEndBounds.right + endBounds.left
        val newEndTop = prevEndBounds.top
        val newEndBounds = Rect(newEndLeft, newEndTop,
                newEndLeft + endBounds.width(),
                newEndTop + endBounds.height())

        return createPointToPointAnimator(sceneRoot, view, startBounds, newEndBounds, 255, 0)
    }

    private fun createDrawableBoundsForView(view: View): DrawableBounds {
        val drawable = BitmapDrawable(view.resources, drawViewToBitmp(view))
        return DrawableBounds(drawable)
    }

    private fun drawViewToBitmp(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
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

    private fun createPointToPointAnimator(
        sceneRoot: ViewGroup,
        view: View,
        startBounds: Rect,
        endBounds: Rect,
        startAlpha: Int = 255,
        endAlpha: Int = 255
    ): Animator {
        val drawable = createDrawableBoundsForView(view)
        ViewUtils.getOverlay(sceneRoot).add(drawable.drawable)

        val moveAnim = createMoveAnimatorForView(drawable, startBounds, endBounds)
        val anim: Animator

        when {
            startAlpha != endAlpha -> {
                val alphaAnim = ObjectAnimator.ofInt(drawable.drawable, DrawableAlphaProperty, startAlpha, endAlpha)
                anim = AnimatorSet().apply { playTogether(moveAnim, alphaAnim) }
            }
            else -> {
                drawable.drawable.alpha = endAlpha
                anim = moveAnim
            }
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ViewUtils.getOverlay(sceneRoot).remove(drawable.drawable)
            }
        })

        return anim
    }

    private fun createMoveAnimatorForView(
        drawableBounds: DrawableBounds,
        startBounds: Rect,
        endBounds: Rect
    ): Animator {

        val outTopLeftPropVal = PropertyValuesHolder.ofObject<PointF>(
                TOP_LEFT_PROPERTY,
                null,
                pathMotion.getPath(
                        startBounds.left.toFloat(), startBounds.top.toFloat(),
                        endBounds.left.toFloat(), endBounds.top.toFloat()))

        val outBottomRightPropVal = PropertyValuesHolder.ofObject<PointF>(
                BOTTOM_RIGHT_PROPERTY,
                null,
                pathMotion.getPath(
                        startBounds.right.toFloat(), startBounds.bottom.toFloat(),
                        endBounds.right.toFloat(), endBounds.bottom.toFloat()))

        return ObjectAnimator.ofPropertyValuesHolder(drawableBounds, outTopLeftPropVal, outBottomRightPropVal)
    }

    private fun findPreviousItemViewWithGreaterRight(
        parent: RecyclerView,
        view: View,
        right: Int
    ): Rect? {
        // This view might not necessarily be the root of the item view
        val rvItemView = parent.findContainingItemView(view)
        for (i in parent.getChildAdapterPosition(rvItemView) - 1 downTo 0) {
            parent.findViewHolderForAdapterPosition(i)?.let {
                getTransitionValues(it.itemView, false)?.let {
                    (it.values[PROPNAME_BOUNDS] as? Rect)?.let {
                        if (it.right > right) {
                            return it
                        }
                    }
                }
            }
        }
        return null
    }

    private class DrawableBounds(val drawable: Drawable) : PointFBounds() {
        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) =
                drawable.setBounds(left, top, right, bottom)
    }

    private abstract class PointFBounds {
        private var left: Int = 0
        private var top: Int = 0
        private var right: Int = 0
        private var bottom: Int = 0
        private var topLeftCalls: Int = 0
        private var bottomRightCalls: Int = 0

        fun setTopLeft(topLeft: PointF) {
            left = Math.round(topLeft.x)
            top = Math.round(topLeft.y)
            topLeftCalls++
            if (topLeftCalls == bottomRightCalls) {
                setLeftTopRightBottom()
            }
        }

        fun setBottomRight(bottomRight: PointF) {
            right = Math.round(bottomRight.x)
            bottom = Math.round(bottomRight.y)
            bottomRightCalls++
            if (topLeftCalls == bottomRightCalls) {
                setLeftTopRightBottom()
            }
        }

        private fun setLeftTopRightBottom() {
            setLeftTopRightBottom(left, top, right, bottom)
            topLeftCalls = 0
            bottomRightCalls = 0
        }

        abstract fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int)
    }

    companion object {
        private const val PROPNAME_BOUNDS = "android:columnedChangeBounds:bounds"
        private const val PROPNAME_PARENT = "android:columnedChangeBounds:parent"
        private val TRANSITION_PROPS = arrayOf(PROPNAME_BOUNDS, PROPNAME_PARENT)

        private val TEMP_ARRAY = IntArray(2)

        private val TOP_LEFT_PROPERTY = object : Property<PointFBounds, PointF>(PointF::class.java, "topLeft") {
            override fun set(bounds: PointFBounds, topLeft: PointF) = bounds.setTopLeft(topLeft)
            override fun get(bounds: PointFBounds): PointF? = null
        }

        private val BOTTOM_RIGHT_PROPERTY = object : Property<PointFBounds, PointF>(PointF::class.java, "bottomRight") {
            override fun set(bounds: PointFBounds, bottomRight: PointF) = bounds.setBottomRight(bottomRight)
            override fun get(bounds: PointFBounds): PointF? = null
        }
    }
}
