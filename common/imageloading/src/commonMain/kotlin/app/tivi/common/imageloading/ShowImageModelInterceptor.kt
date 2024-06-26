// Copyright 2019, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.imagemodels.ShowImageModel
import app.tivi.data.models.ImageType
import app.tivi.data.models.TmdbImageEntity
import app.tivi.data.showimages.ShowImagesStore
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import app.tivi.util.cancellableRunCatching
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.size.pxOrElse
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.impl.extensions.get

@Inject
class ShowImageModelInterceptor(
  private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
  private val showImagesStore: Lazy<ShowImagesStore>,
  private val powerController: Lazy<PowerController>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interceptor {
  override suspend fun intercept(
    chain: Interceptor.Chain,
  ): ImageResult = when (val data = chain.request.data) {
    is ShowImageModel -> handle(chain, data).proceed()
    else -> chain.proceed()
  }

  private suspend fun handle(
    chain: Interceptor.Chain,
    model: ShowImageModel,
  ): Interceptor.Chain {
    val entity = withContext(dispatchers.io) {
      cancellableRunCatching {
        findHighestRatedForType(showImagesStore.value.get(model.id).images, model.imageType)
      }.getOrNull()
    } ?: return chain

    val requestWidth = chain.request.sizeResolver.size().width.pxOrElse { 0 }

    val width = when (powerController.value.shouldSaveData()) {
      is SaveData.Disabled -> requestWidth
      // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
      is SaveData.Enabled -> requestWidth / 2
    }

    val request = chain.request.newBuilder()
      .data(tmdbImageUrlProvider.value.buildUrl(entity, model.imageType, width))
      .build()

    return chain.withRequest(request)
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
