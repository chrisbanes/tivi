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

package app.tivi.ui.motionlayout

import androidx.constraintlayout.motion.widget.MotionLayout

open class TransitionListenerAdapter : MotionLayout.TransitionListener {
    private var lastProgress = 0f

    override fun onTransitionTrigger(
        parent: MotionLayout,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
    }

    override fun onTransitionStarted(
        parent: MotionLayout,
        startId: Int,
        endId: Int
    ) {
        lastProgress = 0f
    }

    final override fun onTransitionChange(
        parent: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
        onTransitionChange(parent, startId, endId, progress, progress > lastProgress)
        lastProgress = progress
    }

    open fun onTransitionChange(
        parent: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float,
        positive: Boolean
    ) = Unit

    override fun onTransitionCompleted(
        parent: MotionLayout,
        currentId: Int
    ) = Unit
}
