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

package app.tivi.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

fun animatorSetOf(vararg animators: Animator, playTogether: Boolean = true) = AnimatorSet().apply {
    if (playTogether) {
        playTogether(*animators)
    } else {
        playSequentially(*animators)
    }
}

fun animatorSetOf(animators: List<Animator>, playTogether: Boolean = true) = AnimatorSet().apply {
    if (playTogether) {
        playTogether(animators)
    } else {
        playSequentially(animators)
    }
}

suspend fun Animator.awaitEnd() = suspendCancellableCoroutine<Unit> { cont ->
    // Add an invokeOnCancellation listener. If the coroutine is
    // cancelled, cancel the animation too that will notify
    // listener's onAnimationCancel() function
    cont.invokeOnCancellation { cancel() }

    addListener(object : AnimatorListenerAdapter() {
        private var endedSuccessfully = true

        override fun onAnimationCancel(animation: Animator) {
            // Animator has been cancelled, so flip the success flag
            endedSuccessfully = false
        }

        override fun onAnimationEnd(animation: Animator) {
            // Make sure we remove the listener so we don't keep leak the coroutine continuation
            animation.removeListener(this)

            if (cont.isActive) {
                // If the coroutine is still active...
                if (endedSuccessfully) {
                    // ..and the Animator ended successfully, resume the coroutine
                    cont.resume(Unit)
                } else {
                    // ...and the Animator was cancelled, cancel the coroutine too
                    cont.cancel()
                }
            }
        }
    })
}
