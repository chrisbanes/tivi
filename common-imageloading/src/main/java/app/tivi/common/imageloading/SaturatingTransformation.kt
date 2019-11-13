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

package app.tivi.common.imageloading

import androidx.core.animation.doOnEnd
import app.tivi.ui.animations.SATURATION_ANIMATION_DURATION
import app.tivi.ui.animations.saturateDrawableAnimator
import coil.annotation.ExperimentalCoil
import coil.transition.Transition
import coil.transition.TransitionResult
import coil.transition.TransitionTarget
import kotlinx.coroutines.suspendCancellableCoroutine

/** A [Transition] that saturates and fades in the new drawable on load */
@ExperimentalCoil
class SaturatingTransformation(
    private val durationMillis: Long = SATURATION_ANIMATION_DURATION
) : Transition {
    init {
        require(durationMillis > 0) { "durationMillis must be > 0." }
    }

    override suspend fun transition(
        target: TransitionTarget<*>,
        result: TransitionResult
    ) {
        // Don't animate if the request was fulfilled by the memory cache.
        if (result is TransitionResult.Success && result.isMemoryCache) {
            target.onSuccess(result.drawable)
            return
        }

        // Animate the drawable and suspend until the animation is completes.
        suspendCancellableCoroutine<Unit> { continuation ->
            when (result) {
                is TransitionResult.Success -> {
                    val animator = saturateDrawableAnimator(result.drawable,
                        durationMillis, target.view)
                    animator.doOnEnd {
                        continuation.resume(Unit) { animator.cancel() }
                    }
                    animator.start()

                    continuation.invokeOnCancellation { animator.cancel() }
                    target.onSuccess(result.drawable)
                }
                is TransitionResult.Error -> target.onError(result.drawable)
            }
        }
    }
}
