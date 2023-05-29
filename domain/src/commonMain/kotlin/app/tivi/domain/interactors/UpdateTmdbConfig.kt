// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.domain.Interactor
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateTmdbConfig(
    private val tmdbManager: TmdbManager,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Unit>() {
    override suspend fun doWork(params: Unit) {
        withContext(dispatchers.io) {
            tmdbManager.refreshConfiguration()
        }
    }
}
