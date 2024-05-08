// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.animation.core.ExperimentalTransitionApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import app.tivi.common.compose.LogCompositions
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.DefaultModelEqualityDelegate
import coil3.compose.EqualityDelegate
import coil3.compose.LocalPlatformContext
import kotlin.math.roundToInt
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalTransitionApi::class)
@Composable
fun AsyncImage(
  model: Any?,
  contentDescription: String?,
  modifier: Modifier = Modifier,
  imageLoader: ImageLoader = SingletonImageLoader.get(LocalPlatformContext.current),
  transform: (AsyncImagePainter.State) -> AsyncImagePainter.State = AsyncImagePainter.DefaultTransform,
  onState: ((AsyncImagePainter.State) -> Unit)? = null,
  alignment: Alignment = Alignment.Center,
  contentScale: ContentScale = ContentScale.Fit,
  alpha: Float = DefaultAlpha,
  colorFilter: ColorFilter? = null,
  filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
  modelEqualityDelegate: EqualityDelegate = DefaultModelEqualityDelegate,
) {
  var loadStartTime by remember { mutableStateOf(Instant.DISTANT_PAST) }

  val transitionState = remember {
    MutableTransitionState<Pair<AsyncImagePainter.State, Instant>>(
      initialState = AsyncImagePainter.State.Empty to loadStartTime,
    )
  }
  val transition = rememberTransition(transitionState, "image fade")
  val imageTransition = transition.updateImageLoadingTransition()

  LogCompositions("AsyncImage", "Main composition: $model")

  coil3.compose.AsyncImage(
    model = model,
    contentDescription = contentDescription,
    imageLoader = imageLoader,
    modifier = modifier,
    transform = { state ->
      transform(state).let { transformed ->
        when (transformed) {
          is AsyncImagePainter.State.Loading -> {
            loadStartTime = Clock.System.now()
            transformed
          }

          is AsyncImagePainter.State.Success -> {
            val newPainter = transformPainter(transformed.painter) {
              val cm = ColorMatrix()
              cm.apply {
                setAlpha(imageTransition.alpha)
                setBrightness(imageTransition.brightness)
                setSaturation(imageTransition.saturation)
              }

              ColorFilter.colorMatrix(cm)
            }
            AsyncImagePainter.State.Success(newPainter, transformed.result)
          }

          else -> transformed
        }
      }.also {
        // Finally update the transition state
        transitionState.targetState = it to loadStartTime
      }
    },
    onState = onState,
    alignment = alignment,
    contentScale = contentScale,
    alpha = alpha,
    colorFilter = colorFilter,
    filterQuality = filterQuality,
    modelEqualityDelegate = modelEqualityDelegate,
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
