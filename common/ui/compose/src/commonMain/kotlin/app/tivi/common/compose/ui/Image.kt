// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutModifier
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.model.ImageRequestBuilder
import com.seiko.imageloader.model.ImageResult
import com.seiko.imageloader.option.SizeResolver
import com.seiko.imageloader.toPainter
import kotlin.math.roundToInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun AsyncImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    onAction: ((ImageAction) -> Unit)? = null,
    requestBuilder: (ImageRequestBuilder.() -> ImageRequestBuilder)? = null,
    imageLoader: ImageLoader = LocalImageLoader.current,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    val sizeResolver = remember { ConstraintsSizeResolver() }
    val lastRequestBuilder by rememberUpdatedState(requestBuilder)

    val request by produceState(
        initialValue = ImageRequest { data(model) },
        key1 = model,
    ) {
        value = ImageRequest {
            data(model)
            size(sizeResolver)
            lastRequestBuilder?.invoke(this)
        }
    }

    var result by remember { mutableStateOf<ImageResultExtra?>(null) }

    LaunchedEffect(imageLoader) {
        var lastStartTime: Instant = Instant.DISTANT_PAST

        snapshotFlow { request }
            .filterNotNull()
            .onStart { lastStartTime = Clock.System.now() }
            .flatMapLatest { imageLoader.async(it) }
            .onEach { onAction?.invoke(it) }
            .filterIsInstance<ImageResult>()
            .collect {
                result = ImageResultExtra(it, lastStartTime)
            }
    }

    val transition = updateImageLoadingTransition(result)

    val colorMatrix by remember(transition) {
        derivedStateOf {
            ColorMatrix().apply {
                setAlpha(transition.alpha)
                setBrightness(transition.brightness)
                setSaturation(transition.saturation)
            }
        }
    }

    ResultImage(
        result = result?.result,
        alignment = alignment,
        contentDescription = contentDescription,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = when {
            colorMatrix != IDENTITY_MATRIX -> ColorFilter.colorMatrix(colorMatrix)
            else -> colorFilter
        },
        modifier = modifier
            .fillMaxSize()
            .then(sizeResolver),
        filterQuality = filterQuality,
    )
}

private val IDENTITY_MATRIX by lazy { ColorMatrix() }

internal data class ImageResultExtra(
    val result: ImageResult,
    val startTime: Instant,
)

@Composable
private fun ResultImage(
    result: ImageResult?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    Image(
        painter = remember(result) {
            when (result) {
                is ImageResult.Bitmap -> {
                    BitmapPainter(
                        image = result.bitmap.asImageBitmap(),
                        filterQuality = filterQuality,
                    )
                }

                is ImageResult.Image -> result.image.toPainter()
                is ImageResult.Painter -> result.painter
                else -> EmptyPainter
            }
        },
        alignment = alignment,
        contentDescription = contentDescription,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        modifier = modifier,
    )
}

private object EmptyPainter : Painter() {
    override val intrinsicSize get() = Size.Unspecified
    override fun DrawScope.onDraw() = Unit
}

/** A [SizeResolver] that computes the size from the constrains passed during the layout phase. */
internal class ConstraintsSizeResolver : SizeResolver, LayoutModifier {

    private val constraints = MutableStateFlow(Constraints())

    override suspend fun Density.size(): Size {
        return constraints.mapNotNull(Constraints::toSizeOrNull).first()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        // Cache the current constraints.
        this@ConstraintsSizeResolver.constraints.value = constraints

        // Measure and layout the content.
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    fun setConstraints(constraints: Constraints) {
        this.constraints.value = constraints
    }
}

@Stable
private fun Constraints.toSizeOrNull() = when {
    isZero -> null
    else -> Size(
        width = if (hasBoundedWidth) maxWidth.toFloat() else 0f,
        height = if (hasBoundedHeight) maxHeight.toFloat() else 0f,
    )
}

@Stable
class ParallaxAlignment(
    private val horizontalBias: () -> Float = { 0f },
    private val verticalBias: () -> Float = { 0f },
) : Alignment {
    override fun align(
        size: IntSize,
        space: IntSize,
        layoutDirection: LayoutDirection,
    ): IntOffset {
        // Convert to Px first and only round at the end, to avoid rounding twice while calculating
        // the new positions
        val centerX = (space.width - size.width).toFloat() / 2f
        val centerY = (space.height - size.height).toFloat() / 2f
        val resolvedHorizontalBias = if (layoutDirection == LayoutDirection.Ltr) {
            horizontalBias()
        } else {
            -1 * horizontalBias()
        }

        val x = centerX * (1 + resolvedHorizontalBias)
        val y = centerY * (1 + verticalBias())
        return IntOffset(x.roundToInt(), y.roundToInt())
    }
}
