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

package app.tivi.extensions

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

fun ViewGroup.beginDelayedTransition(duration: Long = 200) {
    TransitionManager.beginDelayedTransition(this, AutoTransition().apply { setDuration(duration) })
}

fun View.getBounds(rect: Rect) {
    rect.set(left, top, right, bottom)
}

fun View.doOnApplyWindowInsets(
    f: (
        View,
        insets: WindowInsetsCompat,
        initialPadding: ViewDimensions,
        initialMargin: ViewDimensions
    ) -> Unit
) {
    // Create a snapshot of the view's padding state
    val initialPadding = createStateForViewPadding(this)
    val initialMargin = createStateForViewMargin(this)
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding, initialMargin)
        insets
    }
    requestApplyInsetsWhenAttached()
}

/**
 * Call [View.requestApplyInsets] in a safe away. If we're attached it calls it straight-away.
 * If not it sets an [View.OnAttachStateChangeListener] and waits to be attached before calling
 * [View.requestApplyInsets].
 */
fun View.requestApplyInsetsWhenAttached() = doOnAttach {
    it.requestApplyInsets()
}

fun View.doOnAttach(f: (View) -> Unit) {
    if (isAttachedToWindow) {
        f(this)
    } else {
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                f(v)
                removeOnAttachStateChangeListener(this)
            }

            override fun onViewDetachedFromWindow(v: View) {
                removeOnAttachStateChangeListener(this)
            }
        })
    }
}

/**
 * Allows easy listening to layout passing. Return [true] if you need the listener to keep being
 * attached.
 */
inline fun View.doOnLayouts(crossinline action: (view: View) -> Boolean) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            if (!action(view)) {
                view.removeOnLayoutChangeListener(this)
            }
        }
    })
}

private fun createStateForViewPadding(view: View) = ViewDimensions(
        view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom, view.paddingStart,
        view.paddingEnd
)

private fun createStateForViewMargin(view: View): ViewDimensions {
    return (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let {
        ViewDimensions(it.leftMargin, it.topMargin, it.rightMargin, it.bottomMargin,
                it.marginStart, it.marginEnd)
    } ?: ViewDimensions()
}

data class ViewDimensions(
    val left: Int = 0,
    val top: Int = 0,
    val right: Int = 0,
    val bottom: Int = 0,
    val start: Int = 0,
    val end: Int = 0
)
