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
import app.tivi.data.util.inPast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

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

@Composable
internal fun updateImageLoadingTransition(
  result: ImageResultExtra?,
  transitionLoadTimeCutoff: Duration = 80.milliseconds,
  transitionDuration: Duration = 1.seconds,
): ImageLoadingTransition {
  val transition = updateTransition(result, label = "image fade")

  val alpha = transition.animateFloat(
    transitionSpec = {
      val t = targetState
      if (t == null || t.startTime > transitionLoadTimeCutoff.inPast) {
        // If the image was loaded from memory, snap to the end state
        snap()
      } else {
        tween(transitionDuration.inWholeMilliseconds.toInt() / 2)
      }
    },
    targetValueByState = { if (it == null) 0f else 1f },
  )

  val brightness = transition.animateFloat(
    transitionSpec = {
      val t = targetState
      if (t == null || t.startTime > transitionLoadTimeCutoff.inPast) {
        // If the image was loaded from memory, snap to the end state
        snap()
      } else {
        tween(transitionDuration.inWholeMilliseconds.toInt() * 3 / 4)
      }
    },
    targetValueByState = { if (it == null) -0.2f else 0f },
  )

  val saturation = transition.animateFloat(
    transitionSpec = {
      val t = targetState
      if (t == null || t.startTime > transitionLoadTimeCutoff.inPast) {
        // If the image was loaded from memory, snap to the end state
        snap()
      } else {
        tween(transitionDuration.inWholeMilliseconds.toInt())
      }
    },
    targetValueByState = { if (it == null) 0f else 1f },
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
