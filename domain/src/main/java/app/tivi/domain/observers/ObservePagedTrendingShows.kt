/*
 * Copyright 2019 Google LLC
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

package app.tivi.domain.observers

import androidx.paging.PagedList
import app.tivi.data.FlowPagedListBuilder
import app.tivi.data.daos.TrendingDao
import app.tivi.data.resultentities.TrendingEntryWithShow
import app.tivi.domain.PagingInteractor
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePagedTrendingShows @Inject constructor(
    private val trendingShowsDao: TrendingDao
) : PagingInteractor<ObservePagedTrendingShows.Params, TrendingEntryWithShow>() {

    override fun createObservable(params: Params): Flow<PagedList<TrendingEntryWithShow>> {
        return FlowPagedListBuilder(
            trendingShowsDao.entriesDataSource(),
            params.pagingConfig,
            boundaryCallback = params.boundaryCallback
        ).buildFlow()
    }

    data class Params(
        override val pagingConfig: PagedList.Config,
        override val boundaryCallback: PagedList.BoundaryCallback<TrendingEntryWithShow>?
    ) : Parameters<TrendingEntryWithShow>
}
