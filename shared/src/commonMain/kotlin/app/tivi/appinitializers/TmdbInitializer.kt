// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.appinitializers

import app.tivi.domain.interactors.UpdateTmdbConfig
import app.tivi.domain.invoke
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject

@Inject
class TmdbInitializer(
    private val updateTmdbConfig: UpdateTmdbConfig,
    private val dispatchers: AppCoroutineDispatchers,
) : AppInitializer {
    override fun init() {
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch(dispatchers.main) {
            updateTmdbConfig.invoke()
        }
    }
}
