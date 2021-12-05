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

import app.tivi.extensions.bodyOrThrow
import app.tivi.util.AppCoroutineDispatchers
import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbManager @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val tmdbClient: Tmdb
) {
    private val imageProvider = MutableStateFlow(TmdbImageUrlProvider())

    fun getLatestImageProvider() = imageProvider.value

    suspend fun refreshConfiguration() {
        try {
            val response = withContext(dispatchers.io) {
                tmdbClient.configurationService().configuration().awaitResponse()
            }
            onConfigurationLoaded(response.bodyOrThrow())
        } catch (t: Throwable) {
            // TODO
        }
    }

    private fun onConfigurationLoaded(configuration: Configuration) {
        configuration.images?.also { images ->
            val newProvider = TmdbImageUrlProvider(
                images.secure_base_url!!,
                images.poster_sizes ?: emptyList(),
                images.backdrop_sizes ?: emptyList(),
                images.logo_sizes ?: emptyList()
            )
            imageProvider.value = newProvider
        }
    }
}
