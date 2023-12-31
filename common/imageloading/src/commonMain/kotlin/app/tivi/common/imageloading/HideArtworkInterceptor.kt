// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.ShowImageModel
import app.tivi.data.models.TmdbImageEntity
import app.tivi.settings.TiviPreferences
import coil3.annotation.ExperimentalCoilApi
import coil3.intercept.Interceptor
import coil3.request.ErrorResult
import coil3.request.ImageResult
import me.tatarka.inject.annotations.Inject

@Inject
class HideArtworkInterceptor(
  private val preferences: TiviPreferences,
) : Interceptor {
  @OptIn(ExperimentalCoilApi::class)
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult = when {
    preferences.developerHideArtwork && isArtwork(chain.request.data) -> {
      ErrorResult(
        image = null,
        request = chain.request,
        throwable = Exception("Developer setting: hide artwork enabled"),
      )
    }

    else -> chain.proceed()
  }

  private fun isArtwork(data: Any): Boolean = when (data) {
    is String -> data.contains("tmdb.org")
    is EpisodeImageModel -> true
    is ShowImageModel -> true
    is TmdbImageEntity -> true
    else -> false
  }
}
