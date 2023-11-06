// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.imagemodels.SeasonImageModel
import app.tivi.data.util.inPast
import app.tivi.tmdb.TmdbImageUrlProvider
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.model.ImageRequest
import com.seiko.imageloader.model.ImageResult
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.days
import me.tatarka.inject.annotations.Inject

@Inject
class SeasonImageModelInterceptor(
  private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
  private val repository: SeasonsEpisodesRepository,
) : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
    val request = when (val data = chain.request.data) {
      is SeasonImageModel -> handle(chain, data)
      else -> chain.request
    }
    return chain.proceed(request)
  }

  private suspend fun handle(chain: Interceptor.Chain, model: SeasonImageModel): ImageRequest {
    if (repository.needSeasonUpdate(model.id, expiry = 180.days.inPast)) {
      runCatching { repository.updateSeason(model.id) }
    }

    val season = repository.getSeason(model.id)
    return season?.tmdbPosterPath?.let { posterPath ->
      val url = tmdbImageUrlProvider.value.getPosterUrl(
        path = posterPath,
        imageWidth = chain.options.size.width.roundToInt(),
      )

      ImageRequest(chain.request) {
        data(url)
      }
    } ?: chain.request
  }
}
