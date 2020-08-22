/*
 * Copyright 2019 Google LLC
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

package app.tivi.common.imageloading

import app.tivi.data.entities.ImageType
import app.tivi.data.entities.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import coil.annotation.ExperimentalCoilApi
import coil.intercept.Interceptor
import coil.request.ImageResult
import coil.size.PixelSize
import coil.size.Size
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import javax.inject.Inject
import javax.inject.Provider

@ExperimentalCoilApi
class TmdbImageEntityCoilInterceptor @Inject constructor(
    private val tmdbImageUrlProvider: Provider<TmdbImageUrlProvider>,
    private val powerController: PowerController
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

    private fun map(data: TmdbImageEntity, size: Size): HttpUrl {
        val width = if (size is PixelSize) {
            when (powerController.shouldSaveData()) {
                is SaveData.Disabled -> size.width
                // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
                is SaveData.Enabled -> size.width / 2
            }
        } else 0

        val urlProvider = tmdbImageUrlProvider.get()
        return when (data.type) {
            ImageType.BACKDROP -> urlProvider.getBackdropUrl(data.path, width).toHttpUrl()
            ImageType.POSTER -> urlProvider.getPosterUrl(data.path, width).toHttpUrl()
            ImageType.LOGO -> urlProvider.getLogoUrl(data.path, width).toHttpUrl()
        }
    }
}
