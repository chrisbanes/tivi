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
import androidx.ui.core.Alignment
import androidx.ui.core.Modifier
import androidx.ui.core.clipToBounds
import androidx.ui.core.onChildPositioned
import androidx.ui.core.onPositioned
import androidx.ui.core.paint
import androidx.ui.foundation.Box
import androidx.ui.geometry.Offset
import androidx.ui.graphics.Canvas
import androidx.ui.graphics.ImageAsset
import androidx.ui.graphics.Paint
import androidx.ui.graphics.ScaleFit
import androidx.ui.graphics.asImageAsset
import androidx.ui.graphics.painter.ImagePainter
import androidx.ui.graphics.painter.Painter
import androidx.ui.unit.IntPx
import androidx.ui.unit.IntPxSize
import androidx.ui.unit.PxSize
import app.tivi.ui.graphics.ImageLoadingColorMatrix
import coil.Coil
import coil.api.newGetBuilder
import coil.size.Scale
import coil.transform.Transformation
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

/**
 * A composable which loads an image using [Coil] into a [Box], using a crossfade when first
 * loaded.
 */
@Composable
fun LoadNetworkImageWithCrossfade(
    data: Any,
    transformations: List<Transformation> = emptyList(),
    alignment: Alignment = Alignment.Center,
    scaleFit: ScaleFit = ScaleFit.Fit,
    modifier: Modifier = Modifier.None
) {
    var childSize by state { IntPxSize(IntPx.Zero, IntPx.Zero) }
    var imgLoadState by stateFor(data, childSize) { ImageLoadState.Empty }

    Transition(
        definition = imageSaturationTransitionDef,
        toState = imgLoadState
    ) { transitionState ->
        val image = if (childSize.width > IntPx.Zero && childSize.height > IntPx.Zero) {
            // If we have a size, we can now load the image using those bounds...
            loadImage(data, childSize, transformations) {
                // Once loaded, update the load state
                imgLoadState = ImageLoadState.Loaded
            }
        } else null

        var childModifier = modifier.onPositioned { childSize = it.size }

        if (image != null) {
            // Create and update the ImageLoadingColorMatrix from the transition state
            val matrix = remember(image) { ImageLoadingColorMatrix() }
            matrix.saturationFraction = transitionState[saturation]
            matrix.alphaFraction = transitionState[alpha]
            matrix.brightnessFraction = transitionState[brightness]

            // Unfortunately ColorMatrixColorFilter is not mutable so we have to create a new
            // instance every time
            val cf = ColorMatrixColorFilter(matrix)
            childModifier = childModifier.clipToBounds().paint(
                painter = AndroidColorMatrixImagePainter(image, cf),
                scaleFit = scaleFit,
                alignment = alignment
            )
        }

        Box(modifier = childModifier)
    }
}

/**
 * A simple composable which loads an image using [Coil] into a [Box]
 */
@Composable
fun LoadNetworkImage(
    data: Any,
    transformations: List<Transformation> = emptyList(),
    modifier: Modifier = Modifier.None
) {
    var childSize by state { IntPxSize(IntPx.Zero, IntPx.Zero) }

    val image = if (childSize.width > IntPx.Zero && childSize.height > IntPx.Zero) {
        // If we have a size, we can now load the image using those bounds...
        loadImage(data, childSize, transformations)
    } else null

    Box(modifier = modifier.onChildPositioned { childSize = it.size }
        .plus(if (image != null) {
            Modifier.clipToBounds().paint(ImagePainter(image))
        } else {
            Modifier.None
        })
    )
}

/**
 * An [ImagePainter] which draws the image with the given Android framework
 * [android.graphics.ColorFilter].
 */
internal class AndroidColorMatrixImagePainter(
    private val image: ImageAsset,
    colorFilter: android.graphics.ColorFilter
) : Painter() {
    private val paint = Paint()
    private val size = PxSize(IntPx(image.width), IntPx(image.height))

    init {
        paint.asFrameworkPaint().colorFilter = colorFilter
    }

    override fun onDraw(canvas: Canvas, bounds: PxSize) {
        // Always draw the image in the top left as we expect it to be translated and scaled
        // in the appropriate position
        canvas.drawImage(image, Offset.zero, paint)
    }

    /**
     * Return the dimension of the underlying [Image] as it's intrinsic width and height
     */
    override val intrinsicSize: PxSize get() = size
}

/**
 * A simple [loadImage] composable, which loads [data].
 */
@Composable
fun loadImage(
    data: Any,
    pxSize: IntPxSize,
    transformations: List<Transformation> = emptyList(),
    onLoad: () -> Unit = {}
): ImageAsset? {
    val request = remember(data, pxSize) {
        Coil.loader().newGetBuilder()
            .data(data)
            .apply {
                if (pxSize.width > IntPx.Zero && pxSize.height > IntPx.Zero) {
                    size(pxSize.width.value, pxSize.height.value)
                    scale(Scale.FILL)
                }
            }
            .transformations(transformations)
            .build()
    }

    var image by stateFor<ImageAsset?>(request) { null }

    // Execute the following code whenever the request changes.
    onCommit(request) {
        val job = CoroutineScope(Dispatchers.Main.immediate).launch {
            // Start loading the image and await the result.
            val drawable = Coil.loader().get(request)
            image = drawable.toBitmap().asImageAsset()
            onLoad()
        }

        // Cancel the request if the input to onCommit changes or
        // the Composition is removed from the composition tree.
        onDispose { job.cancel() }
    }

    // Emit a null Image to start with.
    return image
}
