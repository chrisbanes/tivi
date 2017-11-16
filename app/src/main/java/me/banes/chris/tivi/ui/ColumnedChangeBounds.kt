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
            values.values.put(PROPNAME_BOUNDS, Rect(view.left, view.top, view.right, view.bottom))
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
            endValues: TransitionValues?): Animator? {
        if (startValues == null || endValues == null) {
            return null
        }

        val startParentVals = startValues.values
        val endParentVals = endValues.values
        val startParent = startParentVals[PROPNAME_PARENT] as ViewGroup
        val endParent = endParentVals[PROPNAME_PARENT] as RecyclerView

        val view = endValues.view
        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect

        val startLeft = startBounds.left.toFloat()
        val endLeft = endBounds.left.toFloat()

        val startTop = startBounds.top.toFloat()
        val endTop = endBounds.top.toFloat()

        val startRight = startBounds.right.toFloat()
        val endRight = endBounds.right.toFloat()

        val startBottom = startBounds.bottom.toFloat()
        val endBottom = endBounds.bottom.toFloat()

        val startWidth = startRight - startLeft
        val startHeight = startBottom - startTop

        val endWidth = endRight - endLeft
        val endHeight = endBottom - endTop

        val anim: Animator

        ViewUtils.setLeftTopRightBottom(view, startBounds.left, startBounds.top, startBounds.right, startBounds.bottom)
        val viewBounds = ViewBounds(view)

        if (endWidth > startWidth && endLeft < startLeft) {
            // If we're bigger, and we're going left, we're probably part of a grid of different column
            // counts
            val anims = mutableListOf<Animator>()

            // Animate a copy of the current view out to the right
            anims += createAnimatorsForChildCopyOut(sceneRoot, view, endParent, startBounds, endBounds)

            // Animate the current view in from the left
            val inStartTop = startBottom
            val inTopLeftPropValue = PropertyValuesHolder.ofObject<PointF>(
                    TOP_LEFT_PROPERTY,
                    null,
                    pathMotion.getPath(
                            endLeft - startWidth,
                            inStartTop,
                            endLeft,
                            endTop))

            val inBottomRightPropValue = PropertyValuesHolder.ofObject<PointF>(
                    BOTTOM_RIGHT_PROPERTY,
                    null,
                    pathMotion.getPath(
                            endLeft,
                            inStartTop + startHeight,
                            endRight,
                            endBottom))

            anims += ObjectAnimator.ofPropertyValuesHolder(viewBounds, inTopLeftPropValue, inBottomRightPropValue)
            anims += ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

            anim = AnimatorSet()
            anim.playTogether(anims)
        } else if (endWidth < startWidth && endLeft > startLeft) {
            // If we're smaller, and we're going right, we're probably part of a grid of different column
            // counts

            val gap = 0 // Fix this spacing
            val anims = mutableListOf<Animator>()

            // Animate the current view in from the right
            val inStartLeft = endRight + gap
            val inStartTop = endTop
            val inTopLeftPropValue = PropertyValuesHolder.ofObject<PointF>(
                    TOP_LEFT_PROPERTY,
                    null,
                    pathMotion.getPath(
                            endRight + gap,
                            inStartTop,
                            endLeft,
                            endTop))

            val inBottomRightPropValue = PropertyValuesHolder.ofObject<PointF>(
                    BOTTOM_RIGHT_PROPERTY,
                    null,
                    pathMotion.getPath(
                            inStartLeft + startWidth,
                            inStartTop + startHeight,
                            endRight,
                            endBottom))

            anims += ObjectAnimator.ofPropertyValuesHolder(viewBounds, inTopLeftPropValue, inBottomRightPropValue)
            anims += ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

            anim = AnimatorSet()
            anim.playTogether(anims)
        } else {
            // We're just doing a simple point-to-point animation
            val topLeftPropVal = PropertyValuesHolder.ofObject<PointF>(
                    TOP_LEFT_PROPERTY,
                    null,
                    pathMotion.getPath(startLeft, startTop, endLeft, endTop))

            val bottomRightPropVal = PropertyValuesHolder.ofObject<PointF>(
                    BOTTOM_RIGHT_PROPERTY,
                    null,
                    pathMotion.getPath(startRight, startBottom, endRight, endBottom))

            anim = ObjectAnimator.ofPropertyValuesHolder(viewBounds, topLeftPropVal, bottomRightPropVal)
        }

        anim.addListener(object : AnimatorListenerAdapter() {
            // We need a strong reference to viewBounds until the
            // animator ends (The ObjectAnimator holds only a weak reference).
            @Suppress("unused")
            val viewBoundsRef = viewBounds
        })

        if (view.parent is ViewGroup) {
            val parent = view.parent as ViewGroup
            ViewGroupUtils.suppressLayout(parent, true)
            addListener(object : TransitionListenerAdapter() {
                private var mCanceled = false

                override fun onTransitionCancel(transition: Transition) {
                    ViewGroupUtils.suppressLayout(parent, false)
                    mCanceled = true
                }

                override fun onTransitionEnd(transition: Transition) {
                    if (!mCanceled) {
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

    private fun createAnimatorsForChildCopyOut(
            sceneRoot: ViewGroup, view: View,
            endParent: RecyclerView,
            startBounds: Rect,
            endBounds: Rect): List<Animator> {
        val bitmap = Bitmap.createBitmap(endBounds.width(), endBounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        val drawable = BitmapDrawable(view.resources, bitmap)
        ViewUtils.getOverlay(sceneRoot).add(drawable)
        val drawableBounds = DrawableBounds(drawable)

        val prevEndBounds = findPreviousItemViewWithGreaterRight(endParent, view, endBounds.right)!!
        val newEndLeft = prevEndBounds.right.toFloat() + endBounds.left
        val newEndTop = prevEndBounds.top.toFloat()

        val outTopLeftPropVal = PropertyValuesHolder.ofObject<PointF>(
                TOP_LEFT_PROPERTY,
                null,
                pathMotion.getPath(
                        startBounds.left.toFloat(), startBounds.top.toFloat(),
                        newEndLeft, newEndTop))

        val outBottomRightPropVal = PropertyValuesHolder.ofObject<PointF>(
                BOTTOM_RIGHT_PROPERTY,
                null,
                pathMotion.getPath(startBounds.right.toFloat(), startBounds.bottom.toFloat(),
                        newEndLeft + endBounds.width(), newEndTop + endBounds.height()))

        val outModeAnim = ObjectAnimator.ofPropertyValuesHolder(drawableBounds,
                outTopLeftPropVal, outBottomRightPropVal)
        outModeAnim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                ViewUtils.getOverlay(sceneRoot).remove(drawableBounds.drawable)
            }
        })

        val outFade = ObjectAnimator.ofInt(drawable, DRAWABLE_ALPHA, 255, 0)

        return listOf(outModeAnim, outFade)
    }

    private fun findPreviousItemViewWithGreaterRight(
            parent: RecyclerView,
            view: View,
            right: Int): Rect? {
        return (parent.getChildAdapterPosition(view) - 1 downTo 0)
                .mapNotNull(parent::findViewHolderForAdapterPosition)
                .mapNotNull { getTransitionValues(it.itemView, false) }
                .mapNotNull { it.values[PROPNAME_BOUNDS] }
                .mapNotNull { it as Rect }
                .firstOrNull { it.right > right }
    }

    private class DrawableBounds(val drawable: Drawable) : PointFBounds() {
        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) =
                drawable.setBounds(left, top, right, bottom)
    }

    private class ViewBounds(val view: View) : PointFBounds() {
        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) =
                ViewUtils.setLeftTopRightBottom(view, left, top, right, bottom)
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
        private const val PROPNAME_BOUNDS = "android:changeBounds:bounds"
        private const val PROPNAME_PARENT = "android:changeBounds:parent"
        private val TRANSITION_PROPS = arrayOf(PROPNAME_BOUNDS, PROPNAME_PARENT)

        private val TOP_LEFT_PROPERTY = object : Property<PointFBounds, PointF>(PointF::class.java, "topLeft") {
            override fun set(bounds: PointFBounds, topLeft: PointF) = bounds.setTopLeft(topLeft)
            override fun get(bounds: PointFBounds): PointF? = null
        }

        private val BOTTOM_RIGHT_PROPERTY = object : Property<PointFBounds, PointF>(PointF::class.java, "bottomRight") {
            override fun set(bounds: PointFBounds, bottomRight: PointF) = bounds.setBottomRight(bottomRight)
            override fun get(bounds: PointFBounds): PointF? = null
        }

        private val DRAWABLE_ALPHA = object : Property<Drawable, Int>(Int::class.java, "drawableAlpha") {
            override fun set(d: Drawable, value: Int) {
                d.alpha = value
            }

            override fun get(d: Drawable): Int = d.alpha
        }
    }
}
