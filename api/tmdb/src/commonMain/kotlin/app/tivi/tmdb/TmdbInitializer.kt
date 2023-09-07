// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.tmdb

import app.tivi.appinitializers.AppInitializer
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbInitializer(
    private val tmdbManager: TmdbManager,
    private val dispatchers: AppCoroutineDispatchers,
) : AppInitializer {
    override fun initialize() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(dispatchers.main) {
            tmdbManager.refreshConfiguration()
        }
    }
}
