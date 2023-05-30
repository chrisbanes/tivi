// Copyright 2019, Google LLC, Christopher Banes and the Tivi project contributors
// SPDX-License-Identifier: Apache-2.0

package app.tivi.domain.observers

import app.tivi.data.models.TiviShow
import app.tivi.data.shows.ShowStore
import app.tivi.domain.SubjectInteractor
import app.tivi.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse

@Inject
class ObserveShowDetails(
    private val showStore: ShowStore,
    private val dispatchers: AppCoroutineDispatchers,
) : SubjectInteractor<ObserveShowDetails.Params, TiviShow>() {

    override fun createObservable(params: Params): Flow<TiviShow> {
        return showStore.stream(StoreReadRequest.cached(params.showId, refresh = false))
            .filter { it is StoreReadResponse.Data }
            .map { it.requireData() }
            .flowOn(dispatchers.computation)
    }

    data class Params(val showId: Long)
}
