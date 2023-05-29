// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.util.inPast
import app.tivi.tmdb.TmdbImageUrlProvider
import coil.intercept.Interceptor
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.size.Size
import coil.size.pxOrElse
import kotlin.time.Duration.Companion.days
import me.tatarka.inject.annotations.Inject
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

@Inject
class EpisodeCoilInterceptor(
    private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
    private val repository: SeasonsEpisodesRepository,
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
            chain.request.newBuilder()
                .data(map(backdropPath, chain.size))
                .build()
        } ?: chain.request
    }

    private fun map(backdropPath: String, size: Size): HttpUrl {
        return tmdbImageUrlProvider.value.getBackdropUrl(
            path = backdropPath,
            imageWidth = size.width.pxOrElse { 0 },
        ).toHttpUrl()
    }
}
