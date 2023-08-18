// Copyright 2023, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.common.imageloading

import android.app.Application
import app.tivi.appinitializers.AppInitializer
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class CoilCleanupInitializer(
    private val application: Application,
    private val dispatchers: AppCoroutineDispatchers,
) : AppInitializer {
    @OptIn(DelicateCoroutinesApi::class)
    override fun initialize() {
        GlobalScope.launch {
            withContext(dispatchers.io) {
                // We delete Coil's image_cache folder to claim back space for the user
                val coilCache = application.cacheDir.resolve("image_cache")
                if (coilCache.exists()) {
                    coilCache.deleteRecursively()
                }
            }
        }
    }
}
