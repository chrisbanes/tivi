// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.interactors

import app.tivi.data.showimages.ShowImagesStore
import app.tivi.data.util.fetch
import app.tivi.domain.Interactor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import me.tatarka.inject.annotations.Inject

@Inject
class UpdateShowImages(
    private val showImagesStore: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
) : Interactor<UpdateShowImages.Params>() {
    override suspend fun doWork(params: Params) {
        withContext(dispatchers.io) {
            showImagesStore.fetch(params.showId, params.forceLoad)
        }
    }

    data class Params(val showId: Long, val forceLoad: Boolean)
}
