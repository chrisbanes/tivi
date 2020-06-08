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

package app.tivi.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper

/**
 * This is a extension class to [MotionLayout] which turns it into a [NestedScrollingChild3].
 * This enables you to use a MotionLayout as a child in a nested scrolling parent
 * (such as CoordinatorLayout).
 *
 * There are certain situations when this is very useful, such as when using a MotionLayout in a
 * BottomSheetDialog. BottomSheetDialog and friends use a CoordinatorLayout
 * internally, so we need to make sure that any nested scrolls reach that parent. MotionLayout
 * is not a [NestedScrollingChild3] itself, meaning that it _only_ consumes nested scrolls. This
 * means that any nested scrolls which happen deeper in the tree (i.e. a child RecyclerView) will
 * be consumed by the [MotionLayout] and not travel further up the tree, breaking the bottom sheet
 * behavior.
 *
 * This implementation is unfortunately not perfect, due the complicated nature of scrolling which
 * MotionLayout supports. A very rough implementation of [canScrollVertically] is provided, but
 * ideally this would be added directly to MotionLayout. The same would need to be done for
 * [canScrollHorizontally].
 */
class NestedChildMotionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), NestedScrollingChild3 {
    private val helper = NestedScrollingChildHelper(this)
    private val tmpArray = IntArray(2)

    init {
        isNestedScrollingEnabled = true
    }

    /**
     * NestedScrollingParent methods
     */
    override fun onStartNestedScroll(
        child: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        val selfStarted = super.onStartNestedScroll(child, target, nestedScrollAxes, type)
        val parentStarted = startNestedScroll(axes, type)
        return parentStarted || selfStarted
    }

    override fun onNestedPreScroll(
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        // Dispatch up to our parent first. This gives the parent hierarchy the first attempt
        // at nested scrolling the dx/dy
        dispatchNestedPreScroll(dx, dy, consumed, null, type)

        if (dx != consumed[0] || dy != consumed[1]) {
            // If there is any dx/dy remaining, let MotionLayout handle it
            val innerConsumed = tmpArray
            super.onNestedPreScroll(
                target, dx - consumed[0], dy - consumed[1],
                innerConsumed, type
            )
            consumed[0] += innerConsumed[0]
            consumed[1] += innerConsumed[1]
        }
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            target, dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed,
            type, consumed
        )

        dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed,
            null,
            type, tmpArray
        )
    }

    override fun onNestedPreFling(
        target: View,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return dispatchNestedPreFling(velocityX, velocityY) ||
            super.onNestedPreFling(target, velocityX, velocityY)
    }

    override fun onNestedFling(
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        return dispatchNestedFling(velocityX, velocityY, consumed) ||
            super.onNestedFling(target, velocityX, velocityY, consumed)
    }

    override fun onStopNestedScroll(
        target: View,
        type: Int
    ) {
        super.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    /**
     * NestedScrollingChild3 methods
     */

    override fun startNestedScroll(axes: Int, type: Int) = helper.startNestedScroll(axes, type)

    override fun dispatchNestedPreScroll(
        dx: Int,
        dy: Int,
        consumed: IntArray?,
        offsetInWindow: IntArray?,
        type: Int
    ) = helper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)

    override fun stopNestedScroll(type: Int) = helper.stopNestedScroll(type)

    override fun hasNestedScrollingParent(type: Int) = helper.hasNestedScrollingParent(type)

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int,
        consumed: IntArray
    ) {
        helper.dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed,
            offsetInWindow, type, consumed
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        offsetInWindow: IntArray?,
        type: Int
    ) = helper.dispatchNestedScroll(
        dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
        offsetInWindow, type
    )

    override fun dispatchNestedPreFling(
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return helper.dispatchNestedPreFling(velocityX, velocityY)
    }

    override fun dispatchNestedFling(
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ) = helper.dispatchNestedFling(velocityX, velocityY, consumed)

    override fun isNestedScrollingEnabled() = helper.isNestedScrollingEnabled

    override fun setNestedScrollingEnabled(enabled: Boolean) {
        helper.isNestedScrollingEnabled = enabled
    }

    override fun canScrollVertically(direction: Int): Boolean {
        // This is what BottomSheetBehavior uses to determine whether to take over the nested scroll
        // or not. Since we can't look into MotionLayout's state, we need to make a lot of
        // assumptions based on the progress value
        return (direction > 0 && progress < 1) || (direction < 0 && progress > 0)
    }
}
