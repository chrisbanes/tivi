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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.tivi.data.daos.FollowedShowsDao
import app.tivi.data.entities.SortOption
import app.tivi.data.resultentities.UpNextEntry
import app.tivi.domain.PagingInteractor
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObservePagedUpNextShows @Inject constructor(
    private val followedShowsDao: FollowedShowsDao,
) : PagingInteractor<ObservePagedUpNextShows.Parameters, UpNextEntry>() {

    override fun createObservable(
        params: Parameters,
    ): Flow<PagingData<UpNextEntry>> = Pager(config = params.pagingConfig) {
        followedShowsDao.pagedUpNextShows()
    }.flow

    data class Parameters(
        val sort: SortOption,
        override val pagingConfig: PagingConfig,
    ) : PagingInteractor.Parameters<UpNextEntry>
}
