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
import android.graphics.PointF
import android.graphics.Rect
import android.support.v4.view.ViewCompat
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
    private val tmpLocation = IntArray(2)

    override fun getTransitionProperties(): Array<String>? = TRANSITION_PROPS

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        if (ViewCompat.isLaidOut(view) || view.width != 0 || view.height != 0) {
            values.values.put(PROPNAME_BOUNDS, Rect(view.left, view.top,
                    view.right, view.bottom))
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
        val endParent = endParentVals[PROPNAME_PARENT] as ViewGroup
        if (startParent == null || endParent == null) {
            return null
        }

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
                    anim = ObjectAnimatorUtils.ofPointF(view, POSITION_PROPERTY,
                            topLeftPath)
                } else {
                    val viewBounds = ViewBounds(view)
                    val topLeftPath = pathMotion.getPath(startLeft.toFloat(), startTop.toFloat(),
                            endLeft.toFloat(), endTop.toFloat())
                    val topLeftAnimator = ObjectAnimatorUtils.ofPointF(viewBounds, TOP_LEFT_PROPERTY, topLeftPath)

                    val bottomRightPath = pathMotion.getPath(startRight.toFloat(), startBottom.toFloat(),
                            endRight.toFloat(), endBottom.toFloat())
                    val bottomRightAnimator = ObjectAnimatorUtils.ofPointF(
                            viewBounds, BOTTOM_RIGHT_PROPERTY, bottomRightPath)

                    anim = AnimatorSet().apply {
                        playTogether(topLeftAnimator, bottomRightAnimator)
                        addListener(object : AnimatorListenerAdapter() {
                            // We need a strong reference to viewBounds until the
                            // animator ends (The ObjectAnimator holds only a weak reference).
                            private val viewBoundsRef = viewBounds
                        })
                    }
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

    private class ViewBounds(private val view: View) {
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
            ViewUtils.setLeftTopRightBottom(view, left, top, right, bottom)
            topLeftCalls = 0
            bottomRightCalls = 0
        }

    }

    companion object {
        private const val PROPNAME_BOUNDS = "android:changeBounds:bounds"
        private const val PROPNAME_PARENT = "android:changeBounds:parent"
        private const val PROPNAME_WINDOW_X = "android:changeBounds:windowX"
        private const val PROPNAME_WINDOW_Y = "android:changeBounds:windowY"
        private val TRANSITION_PROPS = arrayOf(PROPNAME_BOUNDS, PROPNAME_PARENT, PROPNAME_WINDOW_X, PROPNAME_WINDOW_Y)

        private val TOP_LEFT_PROPERTY = object : Property<ViewBounds, PointF>(PointF::class.java, "topLeft") {
            override fun set(viewBounds: ViewBounds, topLeft: PointF) = viewBounds.setTopLeft(topLeft)
            override fun get(viewBounds: ViewBounds): PointF? = null
        }

        private val BOTTOM_RIGHT_PROPERTY = object : Property<ViewBounds, PointF>(PointF::class.java, "bottomRight") {
            override fun set(viewBounds: ViewBounds, bottomRight: PointF) = viewBounds.setBottomRight(bottomRight)
            override fun get(viewBounds: ViewBounds): PointF? = null
        }

        private val BOTTOM_RIGHT_ONLY_PROPERTY = object : Property<View, PointF>(PointF::class.java, "bottomRight") {
            override fun set(view: View, bottomRight: PointF) {
                val left = view.left
                val top = view.top
                val right = Math.round(bottomRight.x)
                val bottom = Math.round(bottomRight.y)
                ViewUtils.setLeftTopRightBottom(view, left, top, right, bottom)
            }

            override fun get(view: View): PointF? = null
        }

        private val TOP_LEFT_ONLY_PROPERTY = object : Property<View, PointF>(PointF::class.java, "topLeft") {
            override fun set(view: View, topLeft: PointF) {
                val left = Math.round(topLeft.x)
                val top = Math.round(topLeft.y)
                val right = view.right
                val bottom = view.bottom
                ViewUtils.setLeftTopRightBottom(view, left, top, right, bottom)
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
    }
}
