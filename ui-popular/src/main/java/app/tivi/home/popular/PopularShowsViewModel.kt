/*
 * Copyright 2017 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.tivi.home.popular

import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.data.entities.Status
import app.tivi.domain.interactors.UpdatePopularShows
import app.tivi.domain.observers.ObservePagedPopularShows
import app.tivi.tmdb.TmdbManager
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.EntryViewModel
import app.tivi.util.Logger
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PopularShowsViewModel @Inject constructor(
    override val dispatchers: AppCoroutineDispatchers,
    override val pagingInteractor: ObservePagedPopularShows,
    private val interactor: UpdatePopularShows,
    override val tmdbManager: TmdbManager,
    override val logger: Logger
) : EntryViewModel<PopularEntryWithShow, ObservePagedPopularShows>() {
    init {
        pagingInteractor(ObservePagedPopularShows.Params(pageListConfig, boundaryCallback))

        refresh()
    }

    override fun callLoadMore(): Flow<Status> {
        return interactor(UpdatePopularShows.Params(UpdatePopularShows.Page.NEXT_PAGE))
    }

    override fun callRefresh(): Flow<Status> {
        return interactor(UpdatePopularShows.Params(UpdatePopularShows.Page.REFRESH))
    }
}