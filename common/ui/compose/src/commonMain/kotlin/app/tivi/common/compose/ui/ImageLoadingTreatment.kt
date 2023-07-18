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
import kotlin.time.Duration
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
    result: ImageResultWithSource?,
    duration: Duration = 1.seconds,
): ImageLoadingTransition {
    val transition = updateTransition(result, label = "image fade")

    val alpha = transition.animateFloat(
        transitionSpec = {
            if (targetState?.source == ImageResultSource.MEMORY) {
                // If the image was loaded from memory, snap to the end state
                snap()
            } else {
                tween(duration.inWholeMilliseconds.toInt() / 2)
            }
        },
        targetValueByState = { if (it == null) 0f else 1f },
    )

    val brightness = transition.animateFloat(
        transitionSpec = {
            if (targetState?.source == ImageResultSource.MEMORY) {
                // If the image was loaded from memory, snap to the end state
                snap()
            } else {
                tween(duration.inWholeMilliseconds.toInt() * 3 / 4)
            }
        },
        targetValueByState = { if (it == null) 0.8f else 1f },
    )

    val saturation = transition.animateFloat(
        transitionSpec = {
            if (targetState?.source == ImageResultSource.MEMORY) {
                // If the image was loaded from memory, snap to the end state
                snap()
            } else {
                tween(duration.inWholeMilliseconds.toInt())
            }
        },
        targetValueByState = { if (it == null) 0f else 1f },
    )

    return remember { ImageLoadingTransition(alpha, brightness, saturation) }
}

fun ColorMatrix.setSaturation(sat: Float): ColorMatrix {
    val invSat = 1 - sat
    val R = 0.213f * invSat
    val G = 0.715f * invSat
    val B = 0.072f * invSat
    this[0, 0] = R + sat
    this[0, 1] = G
    this[0, 2] = B
    this[1, 0] = R
    this[1, 1] = G + sat
    this[1, 2] = B
    this[2, 0] = R
    this[2, 1] = G
    this[2, 2] = B + sat

    return this
}

fun ColorMatrix.setBrightness(brightness: Float): ColorMatrix {
    // We subtract to make the picture look darker, it will automatically clamp
    val darkening = (1 - brightness) * MAX_DARKEN_PERCENTAGE * 255
    this[0, 4] = -darkening
    this[1, 4] = -darkening
    this[2, 4] = -darkening

    return this
}

// This means that we darken the image by 20%
private const val MAX_DARKEN_PERCENTAGE = 0.2f

fun ColorMatrix.setAlpha(alpha: Float): ColorMatrix {
    this[3, 3] = alpha
    return this
}
