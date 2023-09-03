// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import androidx.compose.ui.unit.Density
import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.util.inPast
import app.tivi.tmdb.TmdbImageUrlProvider
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.model.ImageResult
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeImageModelInterceptor(
    private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
    private val repository: SeasonsEpisodesRepository,
    private val density: () -> Density,
) : Interceptor {
    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = when (val data = chain.request.data) {
            is EpisodeImageModel -> handle(chain, data)
            else -> chain.request
        }
        return chain.proceed(request)
    }

    private suspend fun handle(chain: Interceptor.Chain, model: EpisodeImageModel): ImageRequest {
        if (repository.needEpisodeUpdate(model.id, expiry = 180.days.inPast)) {
            runCatching { repository.updateEpisode(model.id) }
        }

        return repository.getEpisode(model.id)?.tmdbBackdropPath?.let { backdropPath ->
            val size = chain.options.sizeResolver.run { density().size() }
            val url = tmdbImageUrlProvider.value.getBackdropUrl(
                path = backdropPath,
                imageWidth = size.width.roundToInt(),
            )

            ImageRequest(chain.request) {
                data(url)
            }
        } ?: chain.request
    }
}
