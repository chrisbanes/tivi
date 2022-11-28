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

import android.app.Application
import app.tivi.appinitializers.AppInitializer
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import javax.inject.Inject
import okhttp3.OkHttpClient

@OptIn(ExperimentalCoilApi::class)
class CoilAppInitializer @Inject constructor(
    private val application: Application,
    private val tmdbImageEntityInterceptor: TmdbImageEntityCoilInterceptor,
    private val episodeEntityInterceptor: EpisodeEntityCoilInterceptor,
    private val okHttpClient: OkHttpClient,
) : AppInitializer {
    override fun init() {
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .components {
                    add(tmdbImageEntityInterceptor)
                    add(episodeEntityInterceptor)
                }
                .okHttpClient(okHttpClient)
                .build()
        }
    }
}
