// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.moviebase.tmdb.Tmdb3
import app.moviebase.tmdb.model.TmdbConfiguration
import app.tivi.inject.ApplicationScope
import kotlinx.coroutines.flow.MutableStateFlow
import me.tatarka.inject.annotations.Inject

@ApplicationScope
@Inject
class TmdbManager(
    private val tmdbClient: Tmdb3,
) {
    private val imageProvider = MutableStateFlow(TmdbImageUrlProvider())

    fun getLatestImageProvider() = imageProvider.value

    suspend fun refreshConfiguration() {
        val response = runCatching {
            tmdbClient.configuration.getApiConfiguration()
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
