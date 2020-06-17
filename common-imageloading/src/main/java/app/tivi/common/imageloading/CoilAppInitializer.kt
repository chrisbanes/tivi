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
import android.content.Context
import app.tivi.appinitializers.AppInitializer
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.CoilUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class CoilAppInitializer @Inject constructor(
    private val tmdbImageEntityMapper: TmdbImageEntityCoilMapper,
    private val episodeEntityMapper: EpisodeCoilMapper,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) : AppInitializer {
    override fun init(application: Application) {
        val coilOkHttpClient = okHttpClient.newBuilder()
            .cache(CoilUtils.createDefaultCache(context))
            .build()
        Coil.setImageLoader(object : ImageLoaderFactory {
            override fun newImageLoader(): ImageLoader = ImageLoader.Builder(application)
                // Hardware bitmaps break with our transitions, disable them for now
                .allowHardware(false)
                // Since we don't use hardware bitmaps, we can pool bitmaps and use a higher
                // ratio of memory
                .bitmapPoolPercentage(0.5)
                .componentRegistry {
                    add(tmdbImageEntityMapper)
                    add(episodeEntityMapper)
                }
                .okHttpClient(coilOkHttpClient)
                .build()
        })
    }
}
