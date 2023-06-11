// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Density
import app.tivi.data.models.TmdbImageEntity
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.PowerController
import app.tivi.util.SaveData
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.model.ImageResult
import kotlin.math.roundToInt
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbImageEntityCoilInterceptor(
    private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
    private val powerController: PowerController,
    private val density: () -> Density,
) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val size = chain.options.sizeResolver.run { density().size() }

        val request = when (val data = chain.request.data) {
            is TmdbImageEntity -> {
                chain.request.newBuilder {
                    data(map(data, size))
                }
            }

            else -> chain.request
        }

        return chain.proceed(request)
    }

    private fun map(data: TmdbImageEntity, size: Size): String {
        val width = when (powerController.shouldSaveData()) {
            is SaveData.Disabled -> size.width.roundToInt()
            // If we can't download hi-res images, we load half-width images (so ~1/4 in size)
            is SaveData.Enabled -> size.width.roundToInt() / 2
        }
        return tmdbImageUrlProvider.value.buildUrl(data, data.type, width)
    }
}
