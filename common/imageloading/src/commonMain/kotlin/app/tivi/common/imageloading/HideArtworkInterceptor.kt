// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import app.tivi.data.imagemodels.EpisodeImageModel
import app.tivi.data.imagemodels.ShowImageModel
import app.tivi.data.models.TmdbImageEntity
import app.tivi.settings.TiviPreferences
import com.seiko.imageloader.intercept.Interceptor
import com.seiko.imageloader.model.ImageResult
import me.tatarka.inject.annotations.Inject

@Inject
class HideArtworkInterceptor(
  private val preferences: TiviPreferences,
) : Interceptor {
  override suspend fun intercept(chain: Interceptor.Chain): ImageResult = when {
    preferences.developerHideArtwork && isArtwork(chain.request.data) -> {
      ImageResult.OfError(Exception("Developer setting: hide artwork enabled"))
    }
    else -> chain.proceed(chain.request)
  }

  private fun isArtwork(data: Any): Boolean = when (data) {
    is String -> data.contains("tmdb.org")
    is EpisodeImageModel -> true
    is ShowImageModel -> true
    is TmdbImageEntity -> true
    else -> false
  }
}
