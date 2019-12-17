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
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.motion.widget.MotionLayout
import java.util.concurrent.CopyOnWriteArrayList

class TiviMotionLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr) {

    private val listeners = CopyOnWriteArrayList<TransitionListener>()

    fun addTransitionListener(listener: TransitionListener) {
        listeners.addIfAbsent(listener)
    }

    fun removeTransitionListener(listener: TransitionListener) {
        listeners.remove(listener)
    }

    init {
        super.setTransitionListener(object : TransitionListener {
            override fun onTransitionTrigger(
                motionLayout: MotionLayout,
                triggerId: Int,
                positive: Boolean,
                progress: Float
            ) {
                listeners.forEach {
                    it.onTransitionTrigger(motionLayout, triggerId, positive, progress)
                }
            }

            override fun onTransitionStarted(motionLayout: MotionLayout, startId: Int, endId: Int) {
                listeners.forEach {
                    it.onTransitionStarted(motionLayout, startId, endId)
                }
            }

            override fun onTransitionChange(
                motionLayout: MotionLayout,
                startId: Int,
                endId: Int,
                progress: Float
            ) {
                listeners.forEach {
                    it.onTransitionChange(motionLayout, startId, endId, progress)
                }
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                listeners.forEach {
                    it.onTransitionCompleted(motionLayout, currentId)
                }
            }
        })
    }

    @Deprecated("Use addTransitionListener instead")
    override fun setTransitionListener(listener: TransitionListener) {
        throw IllegalArgumentException("Use addTransitionListener instead")
    }

    /**
     * Whether this MotionLayout should react to nested scrolls and touch events
     */
    var motionEnabled = true

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return motionEnabled && super.onStartNestedScroll(child, target, axes, type)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !motionEnabled) {
            return false
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && !motionEnabled) {
            return false
        }
        return super.onTouchEvent(event)
    }
}
