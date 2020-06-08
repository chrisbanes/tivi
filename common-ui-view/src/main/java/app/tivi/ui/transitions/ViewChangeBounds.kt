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
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.Rect
import android.util.Property
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.doOnEnd
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.drawToBitmap
import androidx.transition.Transition
import androidx.transition.TransitionListenerAdapter
import androidx.transition.TransitionValues
import app.tivi.extensions.getBounds
import app.tivi.ui.animations.FloatProp
import app.tivi.ui.animations.createFloatProperty
import kotlin.math.roundToInt

/**
 * This transition captures the layout bounds of target views before and after
 * the scene change and animates those changes during the transition.
 *
 * A ChangeBounds transition can be described in a resource file by using the
 * tag `changeBounds`, along with the other standard attributes of Transition.
 */
open class ViewChangeBounds : Transition() {

    override fun getTransitionProperties(): Array<String>? = TRANSITION_PROPS

    private fun captureValues(values: TransitionValues) {
        val view = values.view
        if (ViewCompat.isLaidOut(view) || view.width != 0 || view.height != 0) {
            values.values[PROPNAME_BOUNDS] = Rect().apply { view.getBounds(this) }
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

        val view = endValues.view

        val startBounds = startValues.values[PROPNAME_BOUNDS] as Rect
        val endBounds = endValues.values[PROPNAME_BOUNDS] as Rect

        val anim = createPointToPointAnimator(view, startBounds, endBounds)

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

    fun createPointToPointAnimator(
        view: View,
        startBounds: Rect,
        endBounds: Rect,
        startAlpha: Float = 1f,
        endAlpha: Float = 1f,
        useCopy: Boolean = false
    ): Animator {
        val bounds = if (useCopy) DrawableBounds(view) else ViewBounds(view)

        val originalAlpha = bounds.getAlpha()

        val outTopLeftPropVal = PropertyValuesHolder.ofObject<PointF>(
            TOP_LEFT_PROPERTY,
            null,
            pathMotion.getPath(
                startBounds.left.toFloat(), startBounds.top.toFloat(),
                endBounds.left.toFloat(), endBounds.top.toFloat()
            )
        )

        val outBottomRightPropVal = PropertyValuesHolder.ofObject<PointF>(
            BOTTOM_RIGHT_PROPERTY,
            null,
            pathMotion.getPath(
                startBounds.right.toFloat(), startBounds.bottom.toFloat(),
                endBounds.right.toFloat(), endBounds.bottom.toFloat()
            )
        )

        val alphaPropVal = PropertyValuesHolder.ofFloat(ALPHA_PROPERTY, startAlpha, endAlpha)

        bounds.setup()

        bounds.setLeftTopRightBottom(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom)

        return ObjectAnimator.ofPropertyValuesHolder(
            bounds,
            outTopLeftPropVal,
            outBottomRightPropVal,
            alphaPropVal
        ).apply {
            doOnEnd {
                bounds.setAlpha(originalAlpha)
                bounds.cleanup()
            }
        }
    }

    class DrawableBounds(view: View) : PointFBounds() {
        private val drawable = view.drawToBitmap(Bitmap.Config.ARGB_8888).toDrawable(view.resources)
        private val parent = view.parent as ViewGroup

        override fun setup() {
            parent.overlay.add(drawable)
        }

        override fun cleanup() {
            parent.overlay.remove(drawable)
        }

        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) {
            drawable.setBounds(left, top, right, bottom)
        }

        override fun setAlpha(alpha: Float) {
            drawable.alpha = (alpha * 255).roundToInt().coerceIn(0, 255)
        }

        override fun getAlpha(): Float {
            return (drawable.alpha / 255f).coerceIn(0f, 1f)
        }
    }

    class ViewBounds(val view: View) : PointFBounds() {
        override fun setLeftTopRightBottom(left: Int, top: Int, right: Int, bottom: Int) {
            view.left = left
            view.top = top
            view.right = right
            view.bottom = bottom
        }

        override fun getAlpha(): Float = view.alpha

        override fun setAlpha(alpha: Float) {
            view.alpha = alpha
        }
    }

    abstract class PointFBounds {
        private var left: Int = 0
        private var top: Int = 0
        private var right: Int = 0
        private var bottom: Int = 0
        private var topLeftCalls: Int = 0
        private var bottomRightCalls: Int = 0

        open fun setup() = Unit
        open fun cleanup() = Unit

        abstract fun getAlpha(): Float
        abstract fun setAlpha(alpha: Float)

        fun getTopLeft(): PointF {
            return PointF(left.toFloat(), top.toFloat())
        }

        fun setTopLeft(topLeft: PointF) {
            left = topLeft.x.roundToInt()
            top = topLeft.y.roundToInt()
            topLeftCalls++
            if (topLeftCalls == bottomRightCalls) {
                setLeftTopRightBottom()
            }
        }

        fun getBottomRight(): PointF {
            return PointF(right.toFloat(), bottom.toFloat())
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
        const val PROPNAME_BOUNDS = "tivi:changeBounds:bounds"
        const val PROPNAME_PARENT = "tivi:changeBounds:parent"
        val TRANSITION_PROPS = arrayOf(PROPNAME_BOUNDS, PROPNAME_PARENT)

        val TOP_LEFT_PROPERTY: Property<PointFBounds, PointF> =
            object : Property<PointFBounds, PointF>(PointF::class.java, "topLeft") {
                override fun set(bounds: PointFBounds, topLeft: PointF) = bounds.setTopLeft(topLeft)
                override fun get(bounds: PointFBounds): PointF? = bounds.getTopLeft()
            }

        val BOTTOM_RIGHT_PROPERTY: Property<PointFBounds, PointF> =
            object : Property<PointFBounds, PointF>(PointF::class.java, "bottomRight") {
                override fun set(
                    bounds: PointFBounds,
                    bottomRight: PointF
                ) = bounds.setBottomRight(bottomRight)

                override fun get(bounds: PointFBounds): PointF? = bounds.getBottomRight()
            }

        val ALPHA_PROPERTY: Property<PointFBounds, Float> =
            createFloatProperty(object : FloatProp<PointFBounds>("alpha") {
                override fun set(o: PointFBounds, value: Float) = o.setAlpha(value)
                override fun get(o: PointFBounds): Float = o.getAlpha()
            })
    }
}
