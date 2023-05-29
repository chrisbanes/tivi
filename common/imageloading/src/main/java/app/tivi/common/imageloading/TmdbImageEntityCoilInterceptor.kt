// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.models.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.size.Size
import coil.size.pxOrElse
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbImageEntityCoilInterceptor(
    private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
    private val powerController: PowerController,
) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = when (val data = chain.request.data) {
            is TmdbImageEntity -> {
                chain.request.newBuilder()
                    .data(map(data, chain.size))
                    .build()
            }

            else -> chain.request
        }
        return chain.proceed(request)
    }

    private fun map(data: TmdbImageEntity, size: Size): String {
        val width = when (powerController.shouldSaveData()) {
            is SaveData.Disabled -> size.width.pxOrElse { 0 }
            // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
            is SaveData.Enabled -> size.width.pxOrElse { 0 } / 2
        }
        return tmdbImageUrlProvider.value.buildUrl(data, data.type, width)
    }
}
