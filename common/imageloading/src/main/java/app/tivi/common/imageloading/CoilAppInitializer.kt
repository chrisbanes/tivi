// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import android.app.Application
import app.tivi.appinitializers.AppInitializer
import coil.Coil
import coil.ImageLoader
import me.tatarka.inject.annotations.Inject
import okhttp3.OkHttpClient

@Inject
class CoilAppInitializer(
    private val application: Application,
    private val showImageInterceptor: ShowCoilInterceptor,
    private val episodeEntityInterceptor: EpisodeCoilInterceptor,
    private val tmdbImageEntityInterceptor: TmdbImageEntityCoilInterceptor,
    private val okHttpClient: OkHttpClient,
) : AppInitializer {
    override fun init() {
        Coil.setImageLoader {
            ImageLoader.Builder(application)
                .components {
                    add(showImageInterceptor)
                    add(episodeEntityInterceptor)
                    add(tmdbImageEntityInterceptor)
                }
                .okHttpClient(okHttpClient)
                .build()
        }
    }
}
