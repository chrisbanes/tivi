// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.shows.ShowLastRequestStore
import app.tivi.data.shows.ShowStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.domain.interactors.UpdateShowDetails.Params
import app.tivi.util.AppCoroutineDispatchers
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateShowDetails(
    private val showStore: ShowStore,
    private val lastRequestStore: ShowLastRequestStore,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<Params, Unit>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            showStore.fetch(
                key = params.showId,
                forceFresh = params.forceLoad ||
                    lastRequestStore.isRequestExpired(params.showId, 28.days),
            )
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
