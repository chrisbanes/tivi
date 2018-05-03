/*
 * Copyright 2017 Google, Inc.
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

package me.banes.chris.tivi.tmdb

import com.uwetrottmann.tmdb2.Tmdb
import com.uwetrottmann.tmdb2.entities.Configuration
import kotlinx.coroutines.experimental.channels.ConflatedChannel
import kotlinx.coroutines.experimental.channels.ReceiveChannel
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import me.banes.chris.tivi.extensions.fetchBodyWithRetry
import me.banes.chris.tivi.util.AppCoroutineDispatchers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbManager @Inject constructor(
    private val dispatchers: AppCoroutineDispatchers,
    private val tmdbClient: Tmdb
) {
    private val _imageProvider = ConflatedChannel<TmdbImageUrlProvider>()
    val imageProvider: ReceiveChannel<TmdbImageUrlProvider>
        get() = _imageProvider

    init {
        refreshConfiguration()
    }

    private fun refreshConfiguration() {
        launch(dispatchers.main) {
            val config = withContext(dispatchers.network) {
                tmdbClient.configurationService().configuration().fetchBodyWithRetry()
            }
            onConfigurationLoaded(config)
        }
    }

    private fun onConfigurationLoaded(configuration: Configuration) {
        configuration.images?.let {
            val newProvider = TmdbImageUrlProvider(
                    it.secure_base_url,
                    it.poster_sizes.toTypedArray(),
                    it.backdrop_sizes.toTypedArray())
            _imageProvider.offer(newProvider)
        }
    }
}