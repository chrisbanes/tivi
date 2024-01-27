// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.models.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.size.pxOrElse
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbImageEntityCoilInterceptor(
  private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
  private val powerController: Lazy<PowerController>,
) : Interceptor {
  override suspend fun intercept(
    chain: Interceptor.Chain,
  ): ImageResult = when (val data = chain.request.data) {
    is TmdbImageEntity -> {
      val sizedUrl = map(
        data = data,
        requestWidth = chain.request.sizeResolver.size().width.pxOrElse { 0 },
      )
      val request = chain.request.newBuilder()
        .data(sizedUrl)
        .build()
      chain.withRequest(request).proceed()
    }

    else -> chain.proceed()
  }

  private suspend fun map(data: TmdbImageEntity, requestWidth: Int): String {
    val width = when (powerController.value.shouldSaveData()) {
      is SaveData.Disabled -> requestWidth
      // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
      is SaveData.Enabled -> requestWidth / 2
    }
    return tmdbImageUrlProvider.value.buildUrl(data, data.type, width)
  }
}
