/*
 * Copyright 2018 Google LLC
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

package app.tivi.interactors

import androidx.paging.DataSource
import app.tivi.data.repositories.popularshows.PopularShowsRepository
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.extensions.emptyFlowableList
import app.tivi.interactors.UpdatePopularShows.ExecuteParams
import app.tivi.util.AppCoroutineDispatchers
import app.tivi.util.AppRxSchedulers
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class UpdatePopularShows @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val schedulers: AppRxSchedulers,
    private val popularShowsRepository: PopularShowsRepository
) : PagingInteractor<PopularEntryWithShow>, SubjectInteractor<Unit, ExecuteParams, List<PopularEntryWithShow>>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    init {
        // We don't have params, so lets set Unit to kick off the observable
        setParams(Unit)
    }

    override fun dataSourceFactory(): DataSource.Factory<Int, PopularEntryWithShow> {
        return popularShowsRepository.observeForPaging()
    }

    override fun createObservable(params: Unit): Flowable<List<PopularEntryWithShow>> {
        return popularShowsRepository.observeForFlowable()
                .startWith(emptyFlowableList())
                .subscribeOn(schedulers.io)
    }

    override suspend fun execute(params: Unit, executeParams: ExecuteParams) {
        when (executeParams.page) {
            Page.NEXT_PAGE -> popularShowsRepository.loadNextPage()
            Page.REFRESH -> popularShowsRepository.refresh()
        }
    }

    data class ExecuteParams(val page: Page)

    enum class Page {
        NEXT_PAGE, REFRESH
    }
}
