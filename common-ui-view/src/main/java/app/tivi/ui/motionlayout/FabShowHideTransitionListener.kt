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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FabShowHideTransitionListener(
    private val floatingActionButton: FloatingActionButton,
    private val stateFabOpen: Int,
    private val stateFabClosed: Int
) : TransitionListenerAdapter() {
    override fun onTransitionCompleted(parent: MotionLayout, currentId: Int) {
        when (currentId) {
            stateFabOpen -> {
                if (!floatingActionButton.isOrWillBeShown) {
                    floatingActionButton.show()
                }
            }
            stateFabClosed -> {
                if (!floatingActionButton.isOrWillBeHidden) {
                    floatingActionButton.hide()
                }
            }
        }
    }

    override fun onTransitionChange(
        parent: MotionLayout,
        startId: Int,
        endId: Int,
        progress: Float,
        positive: Boolean
    ) {
        if (startId == stateFabOpen && endId == stateFabClosed) {
            if (progress >= 0.53 && positive && !floatingActionButton.isOrWillBeHidden) {
                floatingActionButton.hide()
            } else if (progress <= 0.47 && !positive && !floatingActionButton.isOrWillBeShown) {
                floatingActionButton.show()
            }
        }
    }
}
