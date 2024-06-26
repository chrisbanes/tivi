// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.episodes.SeasonsEpisodesRepository
import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.util.inPast
import app.tivi.tmdb.TmdbImageUrlProvider
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.cancellableRunCatching
import coil3.intercept.Interceptor
import coil3.request.ImageResult
import coil3.size.pxOrElse
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class EpisodeImageModelInterceptor(
  private val tmdbImageUrlProvider: Lazy<TmdbImageUrlProvider>,
  private val repository: Lazy<SeasonsEpisodesRepository>,
  private val dispatchers: AppCoroutineDispatchers,
) : Interceptor {
  override suspend fun intercept(
    chain: Interceptor.Chain,
  ): ImageResult = when (val data = chain.request.data) {
    is EpisodeImageModel -> handle(chain, data).proceed()
    else -> chain.proceed()
  }

  private suspend fun handle(
    chain: Interceptor.Chain,
    model: EpisodeImageModel,
  ): Interceptor.Chain {
    val episode = withContext(dispatchers.io) {
      if (repository.value.needEpisodeUpdate(model.id, expiry = 180.days.inPast)) {
        cancellableRunCatching { repository.value.updateEpisode(model.id) }
      }
      repository.value.getEpisode(model.id)
    }

    return episode?.tmdbBackdropPath?.let { backdropPath ->
      val url = tmdbImageUrlProvider.value.getBackdropUrl(
        path = backdropPath,
        imageWidth = chain.request.sizeResolver.size().width.pxOrElse { 0 },
      )

      val request = chain.request.newBuilder()
        .data(url)
        .build()

      chain.withRequest(request)
    } ?: chain
  }
}
