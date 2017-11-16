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
        val endHeight = endBottom - endTop

        var numChanges = 0

        if (startWidth != 0 && startHeight != 0 || endWidth != 0 && endHeight != 0) {
            if (startLeft != endLeft || startTop != endTop) ++numChanges
            if (startRight != endRight || startBottom != endBottom) ++numChanges
        }

        if (numChanges > 0) {
            val anim: Animator
            ViewUtils.setLeftTopRightBottom(view, startLeft, startTop, startRight, startBottom)

            if (numChanges == 2) {
                if (startWidth == endWidth && startHeight == endHeight) {
                    val topLeftPath = pathMotion.getPath(startLeft.toFloat(), startTop.toFloat(), endLeft.toFloat(),
                            endTop.toFloat())
                    anim = ObjectAnimatorUtils.ofPointF(view, POSITION_PROPERTY, topLeftPath)
                } else {
                    val viewBounds = ViewBounds(view)
                    anim = AnimatorSet()

                    if (endWidth > startWidth && endLeft < startLeft) {
                        // If we're bigger, and we're going left, we're probably part of a grid of different column
                        // counts

                        // Animate the current one out to the right
                        val outAnim = createAnimatorForChildCopyOut(sceneRoot, view, endParent, startBounds, endBounds)

                        val inStartTop = startBottom.toFloat()

                        // Animate the current in from to the left
                        val inTopLeftAnimator = ObjectAnimatorUtils.ofPointF(
                                viewBounds,
                                TOP_LEFT_PROPERTY,
                                pathMotion.getPath(
                                        endLeft.toFloat() - startWidth,
                                        inStartTop,
                                        endLeft.toFloat(),
                                        endTop.toFloat()))

                        val inBottomRightAnimator = ObjectAnimatorUtils.ofPointF(
                                viewBounds,
                                BOTTOM_RIGHT_PROPERTY,
                                pathMotion.getPath(
                                        endLeft.toFloat(),
                                        inStartTop + startHeight,
                                        endRight.toFloat(), endBottom.toFloat()))

                        val inFade = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f)

                        anim.playTogether(outAnim, inTopLeftAnimator, inBottomRightAnimator, inFade)

                    } else {
                        val topLeftAnimator = ObjectAnimatorUtils.ofPointF(
                                viewBounds,
                                TOP_LEFT_PROPERTY,
                                pathMotion.getPath(startLeft.toFloat(), startTop.toFloat(),
                                        endLeft.toFloat(), endTop.toFloat()))

                        val bottomRightAnimator = ObjectAnimatorUtils.ofPointF(
                                viewBounds,
                                BOTTOM_RIGHT_PROPERTY,
                                pathMotion.getPath(startRight.toFloat(), startBottom.toFloat(),
                                        endRight.toFloat(), endBottom.toFloat()))

                        anim.playTogether(topLeftAnimator, bottomRightAnimator)
                    }

                    anim.addListener(object : AnimatorListenerAdapter() {
                        // We need a strong reference to viewBounds until the
                        // animator ends (The ObjectAnimator holds only a weak reference).
                        @Suppress("unused")
                        val viewBoundsRef = viewBounds
                    })
                }
            } else if (startLeft != endLeft || startTop != endTop) {
                val topLeftPath = pathMotion.getPath(startLeft.toFloat(), startTop.toFloat(),
                        endLeft.toFloat(), endTop.toFloat())
                anim = ObjectAnimatorUtils.ofPointF(view, TOP_LEFT_ONLY_PROPERTY,
                        topLeftPath)
            } else {
                val bottomRight = pathMotion.getPath(startRight.toFloat(), startBottom.toFloat(),
                        endRight.toFloat(), endBottom.toFloat())
                anim = ObjectAnimatorUtils.ofPointF(view, BOTTOM_RIGHT_ONLY_PROPERTY, bottomRight)
            }

            if (view.parent is ViewGroup) {
                val parent = view.parent as ViewGroup
                ViewGroupUtils.suppressLayout(parent, true)
                addListener(object : TransitionListenerAdapter() {
                    internal var mCanceled = false

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

        return null
    }

    private fun createAnimatorForChildCopyOut(
            sceneRoot: ViewGroup, view: View,
            endParent: RecyclerView,
            startBounds: Rect,
            endBounds: Rect) : Animator {
        val bitmap = Bitmap.createBitmap(endBounds.width(), endBounds.height(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        val drawable = BitmapDrawable(view.resources, bitmap)
        ViewUtils.getOverlay(sceneRoot).add(drawable)
        val drawableBounds = DrawableBounds(drawable)

        val prevEndBounds = findPreviousItemViewWithGreaterRight(endParent, view, endBounds.right)!!
        val newEndLeft = prevEndBounds.right.toFloat() + endBounds.left
        val newEndTop = prevEndBounds.top.toFloat()

        val outTopLeftAnim = ObjectAnimatorUtils.ofPointF(
                drawableBounds,
                TOP_LEFT_PROPERTY,
                pathMotion.getPath(
                        startBounds.left.toFloat(), startBounds.top.toFloat(),
                        newEndLeft, newEndTop))

        val outBottomRightAnim = ObjectAnimatorUtils.ofPointF(
                drawableBounds,
                BOTTOM_RIGHT_PROPERTY,
                pathMotion.getPath(startBounds.right.toFloat(), startBounds.bottom.toFloat(),
                        newEndLeft + endBounds.width(), newEndTop + endBounds.height()))

        val outFade = ObjectAnimator.ofInt(drawable, DRAWABLE_ALPHA, 255, 0)

        return AnimatorSet().apply {
            playTogether(outTopLeftAnim, outBottomRightAnim, outFade)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    ViewUtils.getOverlay(sceneRoot).remove(drawableBounds.drawable)
                }
            })
        }
    }

    private fun findPreviousItemViewWithGreaterRight(parent: RecyclerView, view: View, right: Int) : Rect? {
        return (parent.getChildAdapterPosition(view) - 1 downTo 0)
                .mapNotNull(parent::findViewHolderForAdapterPosition)
                .mapNotNull { getTransitionValues(it.itemView, false) }
                .map { it.values[PROPNAME_BOUNDS] as Rect }
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

        private val BOTTOM_RIGHT_ONLY_PROPERTY = object : Property<View, PointF>(PointF::class.java, "bottomRight") {
            override fun set(view: View, bottomRight: PointF) {
                ViewUtils.setLeftTopRightBottom(view,
                        view.left, view.top, Math.round(bottomRight.x), Math.round(bottomRight.y))
            }

            override fun get(view: View): PointF? = null
        }

        private val TOP_LEFT_ONLY_PROPERTY = object : Property<View, PointF>(PointF::class.java, "topLeft") {
            override fun set(view: View, topLeft: PointF) {
                ViewUtils.setLeftTopRightBottom(view,
                        Math.round(topLeft.x), Math.round(topLeft.y), view.right, view.bottom)
            }

            override fun get(view: View): PointF? = null
        }

        private val POSITION_PROPERTY = object : Property<View, PointF>(PointF::class.java, "position") {
            override fun set(view: View, topLeft: PointF) {
                val left = Math.round(topLeft.x)
                val top = Math.round(topLeft.y)
                val right = left + view.width
                val bottom = top + view.height
                ViewUtils.setLeftTopRightBottom(view, left, top, right, bottom)
            }

            override fun get(view: View): PointF? = null
        }

        private val DRAWABLE_ALPHA = object : Property<Drawable, Int>(Int::class.java, "drawableAlpha") {
            override fun set(d: Drawable?, value: Int) {
                d?.alpha = value
            }

            override fun get(d: Drawable?): Int = d?.alpha ?: 0
        }
    }
}
