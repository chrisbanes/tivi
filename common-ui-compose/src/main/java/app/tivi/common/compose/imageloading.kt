/*
 * Copyright 2020 Google LLC
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

package app.tivi.common.compose

import android.graphics.ColorMatrixColorFilter
import androidx.animation.FloatPropKey
import androidx.animation.transitionDefinition
import androidx.compose.Composable
import androidx.compose.onCommit
import androidx.compose.remember
import androidx.compose.state
import androidx.compose.stateFor
import androidx.core.graphics.drawable.toBitmap
import androidx.ui.animation.Transition
import androidx.ui.core.Modifier
import androidx.ui.core.OnChildPositioned
import androidx.ui.graphics.Image
import androidx.ui.layout.Container
import androidx.ui.unit.PxSize
import androidx.ui.unit.minDimension
import androidx.ui.unit.px
import app.tivi.ui.graphics.ImageLoadingColorMatrix
import coil.Coil
import coil.api.newGetBuilder
import coil.size.Scale
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private enum class ImageLoadState {
    Loaded,
    Empty
}

private val alpha = FloatPropKey()
private val brightness = FloatPropKey()
private val saturation = FloatPropKey()

private const val transitionDuration = 1000

private val imageSaturationTransitionDef = transitionDefinition {
    state(ImageLoadState.Empty) {
        this[alpha] = 0f
        this[brightness] = 0.8f
        this[saturation] = 0f
    }
    state(ImageLoadState.Loaded) {
        this[alpha] = 1f
        this[brightness] = 1f
        this[saturation] = 1f
    }

    transition {
        alpha using tween {
            duration = transitionDuration / 2
        }
        brightness using tween {
            duration = (transitionDuration * 0.75f).roundToInt()
        }
        saturation using tween {
            duration = transitionDuration
        }
    }
}

@Composable
fun LoadAndShowImage(
    modifier: Modifier = Modifier.None,
    data: Any,
    crossFadeIn: Boolean = false
) {
    var childSize by state { PxSize.Zero }

    OnChildPositioned(onPositioned = { childSize = it.size }) {
        var imgLoadState by stateFor(data, childSize) { ImageLoadState.Empty }

        val image = if (childSize.minDimension > 0.px) {
            // If we have a size, we can now load the image using those bounds...
            LoadImage(data, childSize) {
                // Once loaded, update the load state
                imgLoadState = ImageLoadState.Loaded
            }
        } else null

        Container(modifier = modifier) {
            if (crossFadeIn) {
                val matrix = remember(image) { ImageLoadingColorMatrix() }
                var colorFilter by stateFor(image) { ColorMatrixColorFilter(matrix) }

                Transition(
                    definition = imageSaturationTransitionDef,
                    toState = imgLoadState
                ) { transitionState ->
                    if (image != null) {
                        // Update the ImageLoadingColorMatrix from the transition state
                        matrix.saturationFraction = transitionState[saturation]
                        matrix.alphaFraction = transitionState[alpha]
                        matrix.brightnessFraction = transitionState[brightness]
                        colorFilter = ColorMatrixColorFilter(matrix)

                        DrawImage(image = image) { paint ->
                            // We have to unwrap to the framework paint instance to use
                            // a ColorMatrixColorFilter, for our ImageLoadingColorMatrix
                            paint.asFrameworkPaint().colorFilter = colorFilter
                        }
                    }
                }
            } else if (image != null) {
                DrawImage(image = image)
            }
        }
    }
}

/**
 * A simple [LoadImage] composable, which loads [data].
 */
@Composable
fun LoadImage(
    data: Any,
    pxSize: PxSize = PxSize.Zero,
    onLoad: () -> Unit
): Image? {
    val request = remember(data, pxSize) {
        Coil.loader().newGetBuilder()
            .data(data)
            .apply {
                if (pxSize.minDimension > 0.px) {
                    size(pxSize.width.value.roundToInt(), pxSize.height.value.roundToInt())
                    scale(Scale.FILL)
                }
            }
            .build()
    }

    var image by stateFor<Image?>(request) { null }

    // Execute the following code whenever the request changes.
    onCommit(request) {
        val job = CoroutineScope(Dispatchers.Main.immediate).launch {
            // Start loading the image and await the result.
            val drawable = Coil.loader().get(request)
            image = AndroidImage(drawable.toBitmap())
            onLoad()
        }

        // Cancel the request if the input to onCommit changes or
        // the Composition is removed from the composition tree.
        onDispose { job.cancel() }
    }

    // Emit a null Image to start with.
    return image
}
