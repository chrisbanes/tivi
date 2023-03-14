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

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.tivi.data.compoundmodels.UpNextEntry
import app.tivi.data.daos.WatchedShowDao
import app.tivi.data.models.SortOption
import app.tivi.domain.PagingInteractor
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject

@Inject
class ObservePagedUpNextShows(
    private val watchedShowsDao: WatchedShowDao,
) : PagingInteractor<ObservePagedUpNextShows.Parameters, UpNextEntry>() {

    override fun createObservable(
        params: Parameters,
    ): Flow<PagingData<UpNextEntry>> = Pager(config = params.pagingConfig) {
        when (params.sort) {
            SortOption.AIR_DATE -> watchedShowsDao.pagedUpNextShowsDateAired()
            else -> watchedShowsDao.pagedUpNextShowsLastWatched()
        }
    }.flow

    data class Parameters(
        val sort: SortOption,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<UpNextEntry>
}
