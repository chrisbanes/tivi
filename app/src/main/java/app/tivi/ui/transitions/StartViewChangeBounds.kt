/*
 * Copyright 2018 Google LLC
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
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.Property
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.drawToBitmap
import androidx.core.view.isVisible
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionValues
import kotlin.math.roundToInt

/**
 * This transition captures the layout bounds of target views before and after
 * the scene change and animates those changes during the transition.
 *
 * A ChangeBounds transition can be described in a resource file by using the
 * tag `changeBounds`, along with the other standard attributes of Transition.
 */
open class StartViewChangeBounds : Transition() {

    override fun getTransitionProperties(): Array<String>? = TRANSITION_PROPS

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        if (ViewCompat.isLaidOut(view) || view.width != 0 || view.height != 0) {
            val loc = TEMP_ARRAY
            view.getLocationOnScreen(loc)

            values.values[PROPNAME_BOUNDS] = Rect(loc[0], loc[1], loc[0] + view.width, loc[1] + view.height)
            values.values[PROPNAME_PARENT] = values.view.parent
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

        val anim = createPointToPointAnimator(sceneRoot, startView, startBounds, endBounds)

        endView.visibility = View.INVISIBLE
        anim.doOnEnd {
            endView.isVisible = true
        }

        if (startView.parent is ViewGroup) {
            val parent = startView.parent as ViewGroup
            parent.suppressLayout(true)

            addListener(object : TransitionListenerAdapter() {
                private var canceled = false

                override fun onTransitionCancel(transition: Transition) {
                    parent.suppressLayout(false)
                    canceled = true
                }

                override fun onTransitionEnd(transition: Transition) {
                    if (!canceled) {
                        parent.suppressLayout(false)
                    }
                    transition.removeListener(this)
                }

                override fun onTransitionPause(transition: Transition) {
                    parent.suppressLayout(false)
                }

                override fun onTransitionResume(transition: Transition) {
                    parent.suppressLayout(false)
                }
            })
        }

        return anim
    }

    fun createPointToPointAnimator(
        sceneRoot: ViewGroup,
        view: View,
        startBounds: Rect,
        endBounds: Rect,
        startAlpha: Int = 255,
        endAlpha: Int = 255
    ): Animator {
        val drawable = createDrawableBoundsForView(view)
        sceneRoot.overlay.add(drawable.drawable)

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

        anim.doOnEnd {
            sceneRoot.overlay.remove(drawable.drawable)
        }

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

    private fun createDrawableBoundsForView(view: View): DrawableBounds {
        val d = view.drawToBitmap(Bitmap.Config.ARGB_8888).toDrawable(view.resources)
        return DrawableBounds(d)
    }

    class DrawableBounds(val drawable: Drawable) : PointFBounds() {
        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) =
                drawable.setBounds(left, top, right, bottom)
    }

    abstract class PointFBounds {
        private var left: Int = 0
        private var top: Int = 0
        private var right: Int = 0
        private var bottom: Int = 0
        private var topLeftCalls: Int = 0
        private var bottomRightCalls: Int = 0

        fun setTopLeft(topLeft: PointF) {
            left = topLeft.x.roundToInt()
            top = topLeft.y.roundToInt()
            topLeftCalls++
            if (topLeftCalls == bottomRightCalls) {
                setLeftTopRightBottom()
            }
        }

        fun setBottomRight(bottomRight: PointF) {
            right = bottomRight.x.roundToInt()
            bottom = bottomRight.y.roundToInt()
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
        const val PROPNAME_BOUNDS = "android:columnedChangeBounds:bounds"
        const val PROPNAME_PARENT = "android:columnedChangeBounds:parent"
        val TRANSITION_PROPS = arrayOf(PROPNAME_BOUNDS, PROPNAME_PARENT)

        val TEMP_ARRAY = IntArray(2)

        val TOP_LEFT_PROPERTY: Property<PointFBounds, PointF> =
                object : Property<PointFBounds, PointF>(PointF::class.java, "topLeft") {
                    override fun set(bounds: PointFBounds, topLeft: PointF) = bounds.setTopLeft(topLeft)
                    override fun get(bounds: PointFBounds): PointF? = null
                }

        val BOTTOM_RIGHT_PROPERTY: Property<PointFBounds, PointF> =
                object : Property<PointFBounds, PointF>(PointF::class.java, "bottomRight") {
                    override fun set(bounds: PointFBounds, bottomRight: PointF) = bounds.setBottomRight(bottomRight)
                    override fun get(bounds: PointFBounds): PointF? = null
                }
    }
}
