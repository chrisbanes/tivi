// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import com.seiko.imageloader.ImageLoader
import com.seiko.imageloader.LocalImageLoader
import com.seiko.imageloader.asImageBitmap
import com.seiko.imageloader.model.ImageAction
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.model.ImageRequestBuilder
import com.seiko.imageloader.model.ImageResult
import com.seiko.imageloader.option.SizeResolver
import com.seiko.imageloader.toPainter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapNotNull

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalAnimationApi::class)
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
    val sizeResolver = ConstraintsSizeResolver()
    val lastRequestBuilder by rememberUpdatedState(requestBuilder)

    val request by produceState(ImageRequest(Unit), model, contentScale) {
        value = ImageRequest {
            data(model)
            size(sizeResolver)
            lastRequestBuilder?.invoke(this)
        }
    }

    var crossfade by remember(request) { mutableStateOf(true) }
    var result by remember { mutableStateOf<ImageResult?>(null) }

    LaunchedEffect(imageLoader) {
        snapshotFlow { request }
            .filterNotNull()
            .flatMapLatest { imageLoader.async(it) }
            .collect { action ->
                onAction?.invoke(action)

                if (action is ImageResult) {
                    // Crossfade if the current result is null
                    crossfade = result == null
                    result = action
                }
            }
    }

    AnimatedContent(
        targetState = result to crossfade,
        transitionSpec = {
            val (_, xfade) = targetState
            when {
                xfade -> fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                else -> {
                    // If it's loaded from the memory cache, don't fade it in
                    EnterTransition.None togetherWith ExitTransition.None
                }
            }
        },
        modifier = modifier,
    ) { (r, _) ->
        ResultImage(
            result = r,
            alignment = alignment,
            contentDescription = contentDescription,
            contentScale = contentScale,
            alpha = alpha,
            colorFilter = colorFilter,
            modifier = Modifier
                .fillMaxSize()
                .then(sizeResolver),
            filterQuality = filterQuality,
        )
    }
}

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
        painter = when (result) {
            is ImageResult.Bitmap -> {
                BitmapPainter(
                    image = result.bitmap.asImageBitmap(),
                    filterQuality = filterQuality,
                )
            }

            is ImageResult.Image -> result.image.toPainter()
            is ImageResult.Painter -> result.painter
            else -> EmptyPainter
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

    private val _constraints = MutableStateFlow(Constraints())

    override suspend fun Density.size(): Size {
        return _constraints.mapNotNull(Constraints::toSizeOrNull).first()
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        // Cache the current constraints.
        _constraints.value = constraints

        // Measure and layout the content.
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }

    fun setConstraints(constraints: Constraints) {
        _constraints.value = constraints
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
