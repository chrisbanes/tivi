// Copyright 2020, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.models.ShowImages
import app.tivi.data.showimages.ShowImagesStore
import app.tivi.data.util.filterForResult
import app.tivi.domain.SubjectInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadRequest

@Inject
class ObserveShowImages(
    private val store: ShowImagesStore,
    private val dispatchers: AppCoroutineDispatchers,
) : SubjectInteractor<ObserveShowImages.Params, ShowImages>() {

    override fun createObservable(params: Params): Flow<ShowImages> {
        return store.stream(StoreReadRequest.cached(params.showId, refresh = false))
            .filterForResult()
            .map { it.requireData() }
            .flowOn(dispatchers.computation)
    }

    data class Params(val showId: Long)
}
