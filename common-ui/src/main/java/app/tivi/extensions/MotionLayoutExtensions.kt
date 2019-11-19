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

import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.constraintlayout.widget.ConstraintSet
import app.tivi.ui.widget.TiviMotionLayout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Applies the given function over each [ConstraintSet]
 */
inline fun MotionLayout.updateConstraintSets(f: ConstraintSet.() -> Unit) {
    for (id in constraintSetIds) {
        updateState(id, getConstraintSet(id).apply { f() })
    }
}

/**
 * Wait for the transition to complete so that the given [transitionId] is fully displayed.
 */
suspend fun TiviMotionLayout.awaitTransitionComplete(transitionId: Int) {
    // If we're already at the specified state, return now
    if (currentState == transitionId) return

    suspendCoroutine<Unit> { cont ->
        addTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                if (currentId == transitionId) {
                    removeTransitionListener(this)
                    cont.resume(Unit)
                }
            }
        })
    }
}