/*
 * Copyright 2018 Google, Inc.
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

import android.arch.paging.DataSource
import app.tivi.data.repositories.popularshows.PopularShowsRepository
import app.tivi.data.resultentities.PopularEntryWithShow
import app.tivi.util.AppCoroutineDispatchers
import io.reactivex.Flowable
import kotlinx.coroutines.experimental.CoroutineDispatcher
import javax.inject.Inject

class UpdatePopularShows @Inject constructor(
    dispatchers: AppCoroutineDispatchers,
    private val popularShowsRepository: PopularShowsRepository
) : PagingInteractor<UpdatePopularShows.Params, PopularEntryWithShow>,
        SubjectInteractor<UpdatePopularShows.Params, List<PopularEntryWithShow>>() {
    override val dispatcher: CoroutineDispatcher = dispatchers.io

    override fun dataSourceFactory(): DataSource.Factory<Int, PopularEntryWithShow> {
        return popularShowsRepository.observeForPaging()
    }

    override fun createObservable(param: Params): Flowable<List<PopularEntryWithShow>> {
        return popularShowsRepository.observeForFlowable()
    }

    override suspend fun execute(param: Params) {
        when (param.page) {
            Page.NEXT_PAGE -> popularShowsRepository.loadNextPage()
            Page.REFRESH -> popularShowsRepository.refresh()
        }
    }

    data class Params(val page: Page)

    enum class Page {
        NEXT_PAGE, REFRESH
    }
}
