// Copyright 2024, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.compose

import androidx.collection.lruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import app.tivi.util.cancellableRunCatching
import coil3.Image
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.annotation.ExperimentalCoilApi
import coil3.request.ImageRequest
import coil3.size.Size
import coil3.size.SizeResolver
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.ktx.themeColors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import me.tatarka.inject.annotations.Inject

@OptIn(ExperimentalCoilApi::class)
@Inject
class ColorExtractor(
  private val imageLoader: Lazy<ImageLoader>,
  private val platformContext: Lazy<PlatformContext>,
) {
  private val cache = lruCache<Any, Color>(100)

  suspend fun calculatePrimaryColor(
    model: Any,
    sizeResolver: SizeResolver = DEFAULT_REQUEST_SIZE,
  ): Result {
    val cached = cache[model]
    if (cached != null) {
      return Result(cached, true)
    }

    val bitmap = suspendCancellableCoroutine<ImageBitmap> { cont ->
      val request = ImageRequest.Builder(platformContext.value)
        .data(model)
        .size(sizeResolver)
        .prepareForColorExtractor()
        .target(
          onSuccess = { result ->
            cont.resume(result.toComposeImageBitmap())
          },
          onError = {
            cont.resumeWithException(IllegalArgumentException())
          },
        )
        .build()

      imageLoader.value.enqueue(request)
    }

    val suitableColors = bitmap.themeColors()
    return suitableColors.first()
      .also { cache.put(model, it) }
      .let { Result(it, false) }
  }

  data class Result(val color: Color, val cached: Boolean)

  private companion object {
    val DEFAULT_REQUEST_SIZE = SizeResolver(Size(96, 96))
  }
}

@Composable
fun DynamicTheme(
  model: Any,
  fallback: Color,
  useDarkTheme: Boolean = false,
  style: PaletteStyle = PaletteStyle.TonalSpot,
  content: @Composable () -> Unit,
) {
  val colorExtractor = LocalColorExtractor.current

  val result by produceState<ColorExtractor.Result?>(initialValue = null, model, colorExtractor) {
    val result = cancellableRunCatching {
      colorExtractor.calculatePrimaryColor(model)
    }
    value = result.getOrNull()
  }

  DynamicMaterialTheme(
    seedColor = result?.color ?: fallback,
    useDarkTheme = useDarkTheme,
    animate = result.let { it != null && !it.cached },
    style = style,
    content = content,
  )
}

internal expect fun ImageRequest.Builder.prepareForColorExtractor(): ImageRequest.Builder

internal expect fun Image.toComposeImageBitmap(): ImageBitmap
