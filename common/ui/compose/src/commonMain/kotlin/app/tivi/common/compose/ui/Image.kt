// Copyright 2022, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import app.tivi.common.compose.LogCompositions
import app.tivi.data.imagemodels.ShowImageModel
import app.tivi.data.imagemodels.asImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TiviShow
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.compose.AsyncImagePainter
import coil3.compose.DefaultModelEqualityDelegate
import coil3.compose.EqualityDelegate
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import kotlin.math.roundToInt

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
  LogCompositions("AsyncImage", "Main composition: $model")

  val context = LocalPlatformContext.current

  coil3.compose.AsyncImage(
    model = remember(context, model) {
      ImageRequest.Builder(context)
        .data(model)
        .crossfade(300)
        .build()
    },
    contentDescription = contentDescription,
    imageLoader = imageLoader,
    modifier = modifier,
    transform = transform,
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

@Composable
fun rememberShowImageModel(show: TiviShow, imageType: ImageType): ShowImageModel {
  return remember(show.id, imageType) { show.asImageModel(imageType) }
}
