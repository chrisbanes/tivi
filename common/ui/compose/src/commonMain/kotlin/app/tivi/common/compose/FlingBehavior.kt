// Copyright 2021, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.snapping.SnapFlingBehavior
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.platform.LocalDensity
import kotlin.math.abs
import kotlinx.coroutines.withContext

@Composable
fun rememberTiviFlingBehavior(): FlingBehavior {
  val decayAnimationSpec = decayAnimationSpec()
  return remember(decayAnimationSpec) {
    DefaultFlingBehavior(decayAnimationSpec)
  }
}

@ExperimentalFoundationApi
@Composable
fun rememberTiviSnapFlingBehavior(
  snapLayoutInfoProvider: SnapLayoutInfoProvider,
): SnapFlingBehavior {
  val density = LocalDensity.current
  val highVelocityApproachSpec: DecayAnimationSpec<Float> = rememberTiviDecayAnimationSpec()
  return remember(snapLayoutInfoProvider, highVelocityApproachSpec, density) {
    SnapFlingBehavior(
      snapLayoutInfoProvider = snapLayoutInfoProvider,
      lowVelocityAnimationSpec = tween(easing = LinearEasing),
      highVelocityAnimationSpec = highVelocityApproachSpec,
      snapAnimationSpec = spring(stiffness = Spring.StiffnessMediumLow),
      density = density,
    )
  }
}

@Composable
fun rememberTiviDecayAnimationSpec(): DecayAnimationSpec<Float> {
  val spec = decayAnimationSpec()
  return remember { spec }
}

@Composable
internal expect fun decayAnimationSpec(): DecayAnimationSpec<Float>

internal val DefaultScrollMotionDurationScale = object : MotionDurationScale {
  override val scaleFactor: Float get() = 1f
}

internal class DefaultFlingBehavior(
  private val flingDecay: DecayAnimationSpec<Float>,
  private val motionDurationScale: MotionDurationScale = DefaultScrollMotionDurationScale,
) : FlingBehavior {
  override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
    // come up with the better threshold, but we need it since spline curve gives us NaNs
    return withContext(motionDurationScale) {
      if (abs(initialVelocity) > 1f) {
        var velocityLeft = initialVelocity
        var lastValue = 0f
        AnimationState(
          initialValue = 0f,
          initialVelocity = initialVelocity,
        ).animateDecay(flingDecay) {
          val delta = value - lastValue
          val consumed = scrollBy(delta)
          lastValue = value
          velocityLeft = this.velocity
          // avoid rounding errors and stop if anything is unconsumed
          if (abs(delta - consumed) > 0.5f) cancelAnimation()
        }
        velocityLeft
      } else {
        initialVelocity
      }
    }
  }
}
