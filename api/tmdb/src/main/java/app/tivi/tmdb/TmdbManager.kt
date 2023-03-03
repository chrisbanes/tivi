/*
 * Copyright 2017 Google LLC
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
        val response = tmdbClient.configuration.getApiConfiguration()
        onConfigurationLoaded(response)
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
