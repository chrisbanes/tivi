// Copyright 2019, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.imagemodels.ShowImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TmdbImageEntity
import app.tivi.data.showimages.ShowImagesStore
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.model.ImageResult
import kotlin.math.roundToInt
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.impl.extensions.get

@Inject
class ShowImageModelInterceptor(
  private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
  private val showImagesStore: ShowImagesStore,
  private val powerController: PowerController,
) : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val request = when (val data = chain.request.data) {
      is ShowImageModel -> handle(chain, data)
      else -> chain.request
    }
    return chain.proceed(request)
  }

  private suspend fun handle(
    chain: Interceptor.Chain,
    model: ShowImageModel,
  ): ImageRequest {
    val entity = runCatching {
      findHighestRatedForType(showImagesStore.get(model.id).images, model.imageType)
    }.getOrNull()

    return if (entity != null) {
      val size = chain.options.size
      val width = when (powerController.shouldSaveData()) {
        is SaveData.Disabled -> size.width.roundToInt()
        // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
        is SaveData.Enabled -> chain.options.size.width.roundToInt() / 2
      }

      ImageRequest(chain.request) {
        data(tmdbImageUrlProvider.value.buildUrl(entity, model.imageType, width))
      }
    } else {
      chain.request
    }
  }
}

internal fun findHighestRatedForType(
  images: List<TmdbImageEntity>,
  type: ImageType,
): TmdbImageEntity? = images.asSequence()
  .filter { it.type == type }
  .maxByOrNull { it.rating + (if (it.isPrimary) 10f else 0f) }

internal fun TmdbImageUrlProvider.buildUrl(
  data: TmdbImageEntity,
  imageType: ImageType,
  width: Int,
): String = when (imageType) {
  ImageType.BACKDROP -> getBackdropUrl(data.path, width)
  ImageType.POSTER -> getPosterUrl(data.path, width)
  ImageType.LOGO -> getLogoUrl(data.path, width)
}
