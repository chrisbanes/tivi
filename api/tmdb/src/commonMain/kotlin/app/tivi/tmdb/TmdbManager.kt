// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbConfiguration
import app.tivi.inject.ApplicationScope
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.cancellableRunCatching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class TmdbManager(
  private val tmdbClient: Lazy<Tmdb3>,
  private val dispatchers: AppCoroutineDispatchers,
) {
  private val imageProvider = MutableStateFlow(TmdbImageUrlProvider())

  fun getLatestImageProvider() = imageProvider.value

  suspend fun refreshConfiguration() {
    val response = withContext(dispatchers.io) {
      cancellableRunCatching {
        tmdbClient.value.configuration.getApiConfiguration()
      }
    }

    if (response.isSuccess) {
      onConfigurationLoaded(response.getOrThrow())
    }
  }

  private fun onConfigurationLoaded(configuration: TmdbConfiguration) {
    configuration.images.also { images ->
      val newProvider = TmdbImageUrlProvider(
        baseImageUrl = images.secureBaseUrl,
        posterSizes = images.posterSizes,
        backdropSizes = images.backdropSizes,
        logoSizes = images.logoSizes,
      )
      imageProvider.value = newProvider
    }
  }
}
