// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ColorMatrix
import app.tivi.data.util.durationSinceNow
import coil3.compose.AsyncImagePainter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.datetime.Instant

@Stable
internal class ImageLoadingTransition(
  alpha: State<Float>,
  brightness: State<Float>,
  saturation: State<Float>,
) {
  val alpha by alpha
  val brightness by brightness
  val saturation by saturation
}

private fun useTransition(
  state: AsyncImagePainter.State,
  startTime: Instant,
  cutOff: Duration,
): Boolean = when {
  state.isFinalState() -> startTime.durationSinceNow() > cutOff
  else -> false
}

@Composable
internal fun updateImageLoadingTransition(
  state: AsyncImagePainter.State,
  startTime: Instant,
  transitionLoadTimeCutoff: Duration = 80.milliseconds,
  transitionDuration: Duration = 1.seconds,
): ImageLoadingTransition {
  val transition = updateTransition(state to startTime, label = "image fade")

  val alpha = transition.animateFloat(
    transitionSpec = {
      val (s, time) = targetState
      if (useTransition(s, time, transitionLoadTimeCutoff)) {
        tween(transitionDuration.inWholeMilliseconds.toInt() / 2)
      } else {
        snap()
      }
    },
    targetValueByState = { (state, _) ->
      if (state.isFinalState()) 1f else 0f
    },
  )

  val brightness = transition.animateFloat(
    transitionSpec = {
      val (s, time) = targetState
      if (useTransition(s, time, transitionLoadTimeCutoff)) {
        tween(transitionDuration.inWholeMilliseconds.toInt() * 3 / 4)
      } else {
        snap()
      }
    },
    targetValueByState = { (state, _) ->
      if (state.isFinalState()) 0f else -0.2f
    },
  )

  val saturation = transition.animateFloat(
    transitionSpec = {
      val (s, time) = targetState
      if (useTransition(s, time, transitionLoadTimeCutoff)) {
        tween(transitionDuration.inWholeMilliseconds.toInt())
      } else {
        snap()
      }
    },
    targetValueByState = { (state, _) ->
      if (state.isFinalState()) 1f else 0f
    },
  )

  return remember { ImageLoadingTransition(alpha, brightness, saturation) }
}

fun ColorMatrix.setSaturation(sat: Float): ColorMatrix {
  val invSat = 1 - sat
  val red = 0.213f * invSat
  val green = 0.715f * invSat
  val blue = 0.072f * invSat
  this[0, 0] = red + sat
  this[0, 1] = green
  this[0, 2] = blue
  this[1, 0] = red
  this[1, 1] = green + sat
  this[1, 2] = blue
  this[2, 0] = red
  this[2, 1] = green
  this[2, 2] = blue + sat

  return this
}

private fun AsyncImagePainter.State.isFinalState(): Boolean {
  return this is AsyncImagePainter.State.Success || this is AsyncImagePainter.State.Error
}

fun ColorMatrix.setBrightness(value: Float): ColorMatrix {
  this[0, 4] = value * 255
  this[1, 4] = value * 255
  this[2, 4] = value * 255
  return this
}

fun ColorMatrix.setAlpha(alpha: Float): ColorMatrix {
  this[3, 3] = alpha
  return this
}
