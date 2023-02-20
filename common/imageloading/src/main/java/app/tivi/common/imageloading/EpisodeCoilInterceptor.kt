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

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.models.Episode
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
            repository.updateEpisode(model.id)
        }

        val episode = repository.getEpisode(model.id)
        return if (episode?.tmdbBackdropPath != null) {
            chain.request.newBuilder()
                .data(map(episode, chain.size))
                .build()
        } else {
            chain.request
        }
    }

    private fun map(data: Episode, size: Size): HttpUrl {
        return tmdbImageUrlProvider.value.getBackdropUrl(
            path = data.tmdbBackdropPath!!,
            imageWidth = size.width.pxOrElse { 0 },
        ).toHttpUrl()
    }
}
