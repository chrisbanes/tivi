// Copyright 2017, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.tivi.inject.ApplicationScope
import me.tatarka.inject.annotations.Provides

interface TmdbComponent : TmdbCommonComponent, TmdbPlatformComponent

expect interface TmdbPlatformComponent

interface TmdbCommonComponent {
    @ApplicationScope
    @Provides
    fun provideTmdbImageUrlProvider(tmdbManager: TmdbManager): TmdbImageUrlProvider {
        return tmdbManager.getLatestImageProvider()
    }
}
